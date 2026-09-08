package com.hkm.stickhub.cloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.io.RandomAccessFile
import org.json.JSONArray
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

data class CloudBackupMetadata(
    val backupId: String,
    val byteSize: Long,
    val checksum: String,
    val createdAt: Long
)

data class CloudBackupDownload(
    val payload: ByteArray,
    val metadata: CloudBackupMetadata
)

class CloudBackupHttpException(val statusCode: Int, message: String) : IOException(message)

/** Small dependency-free HTTP client for the StickHub Cloudflare Worker. */
class CloudBackupClient(
    private val baseUrl: String = DEFAULT_BASE_URL
) {
    suspend fun uploadFile(credentials: CloudVaultCredentials, file: File, backupId: String, checksum: String, progress: (Int, Int) -> Unit): CloudBackupMetadata = withContext(Dispatchers.IO) {
        val path = "/v2/vaults/${credentials.vaultId}/uploads/$backupId/"
        suspend fun control(action: String, body: JSONObject): JSONObject = retry {
            parseJson(request("POST", path + action, credentials, body.toString().toByteArray(), "application/json")).getJSONObject("data")
        }
        val init = control("init", JSONObject().put("byteSize", file.length()).put("checksum", checksum))
        val partSize = init.getLong("partSize")
        check(partSize >= 5L * 1024 * 1024) { "Invalid multipart size." }
        val count = ((file.length() + partSize - 1) / partSize).toInt()
        val parts = JSONArray()
        for (part in 1..count) {
            currentCoroutineContext().ensureActive()
            val offset = (part - 1) * partSize
            val length = minOf(partSize, file.length() - offset)
            val etag = retry {
                val signedUrl = control("part", JSONObject().put("partNumber", part)).getString("url")
                val connection = (URL(signedUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "PUT"
                    connectTimeout = 30_000
                    readTimeout = 120_000
                    doOutput = true
                    instanceFollowRedirects = false
                    setFixedLengthStreamingMode(length)
                }
                try {
                    RandomAccessFile(file, "r").use { input ->
                        input.seek(offset)
                        connection.outputStream.use { output ->
                            val buffer = ByteArray(64 * 1024)
                            var left = length
                            while (left > 0) {
                                currentCoroutineContext().ensureActive()
                                val read = input.read(buffer, 0, minOf(buffer.size.toLong(), left).toInt())
                                check(read > 0) { "Backup file is incomplete." }
                                output.write(buffer, 0, read)
                                left -= read
                            }
                        }
                    }
                    if (connection.responseCode !in 200..299) throw IOException("R2 part upload failed (${connection.responseCode}).")
                    connection.getHeaderField("ETag") ?: throw IOException("R2 upload receipt is missing.")
                } finally { connection.disconnect() }
            }
            parts.put(JSONObject().put("partNumber", part).put("etag", etag))
            progress(part, count)
        }
        val data = control("complete", JSONObject().put("parts", parts))
        CloudBackupMetadata(data.getString("backupId"), data.getLong("byteSize"), data.getString("checksum"), data.getLong("createdAt"))
    }

    suspend fun downloadFile(credentials: CloudVaultCredentials, file: File): CloudBackupMetadata = withContext(Dispatchers.IO) {
        retry {
            val connection = (URL(baseUrl.trimEnd('/') + "/v1/vaults/${credentials.vaultId}/backup").openConnection() as HttpURLConnection).apply {
                connectTimeout = 30_000
                readTimeout = 120_000
                setRequestProperty("X-Vault-Secret-Hash", credentials.secretHashHex)
                setRequestProperty("User-Agent", "StickHub-Android-CloudBackup/2")
                setRequestProperty("Accept-Encoding", "identity")
            }
            try {
                if (connection.responseCode !in 200..299) {
                    val message = connection.errorStream?.bufferedReader()?.use { it.readText() }?.let {
                        runCatching { JSONObject(it).getJSONObject("error").getString("message") }.getOrNull()
                    } ?: "Cloud download failed (${connection.responseCode})."
                    throw CloudBackupHttpException(connection.responseCode, message)
                }
                val id = connection.getHeaderField("X-Backup-Id") ?: throw IOException("Missing backup id.")
                val checksum = connection.getHeaderField("X-Backup-Checksum") ?: throw IOException("Missing backup checksum.")
                val size = connection.getHeaderField("Content-Length")?.toLongOrNull() ?: throw IOException("Missing backup size.")
                connection.inputStream.use { input -> file.outputStream().buffered().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var written = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        written += read
                        check(written <= size) { "Cloud backup size mismatch." }
                        output.write(buffer, 0, read)
                    }
                    check(written == size) { "Cloud download was interrupted." }
                } }
                CloudBackupMetadata(id, size, checksum, connection.getHeaderField("X-Backup-Created-At")?.toLongOrNull() ?: 0L)
            } finally { connection.disconnect() }
        }
    }

    private suspend fun <T> retry(block: suspend () -> T): T {
        repeat(3) { attempt ->
            try { return block() } catch (error: IOException) {
                if (attempt == 2 || (error is CloudBackupHttpException && error.statusCode in 400..499 && error.statusCode != 429)) throw error
                delay(1000L * (attempt + 1))
            }
        }
        error("Upload failed.")
    }
    suspend fun registerVault(credentials: CloudVaultCredentials): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("vaultId", credentials.vaultId)
            .put("secretHash", credentials.secretHashHex)
            .toString()
            .toByteArray(Charsets.UTF_8)
        val response = request(
            method = "POST",
            path = "/v1/vaults",
            credentials = null,
            body = body,
            contentType = "application/json"
        )
        response.code in 200..299
    }

    suspend fun upload(credentials: CloudVaultCredentials, payload: ByteArray, backupId: String, checksum: String): CloudBackupMetadata =
        withContext(Dispatchers.IO) {
            val response = request(
                method = "PUT",
                path = "/v1/vaults/${credentials.vaultId}/backup",
                credentials = credentials,
                body = payload,
                contentType = "application/octet-stream",
                extraHeaders = mapOf(
                    "X-Backup-Id" to backupId,
                    "X-Backup-Checksum" to checksum
                )
            )
            val root = parseJson(response)
            val data = root.getJSONObject("data")
            CloudBackupMetadata(
                backupId = data.getString("backupId"),
                byteSize = data.getLong("byteSize"),
                checksum = data.getString("checksum"),
                createdAt = data.getLong("createdAt")
            )
        }

    suspend fun download(credentials: CloudVaultCredentials): CloudBackupDownload = withContext(Dispatchers.IO) {
        val response = request(
            method = "GET",
            path = "/v1/vaults/${credentials.vaultId}/backup",
            credentials = credentials
        )
        val backupId = response.backupId
            ?: throw IOException("Cloud backup response is missing its id")
        val checksum = response.checksum
            ?: throw IOException("Cloud backup response is missing its checksum")
        val createdAt = response.createdAt ?: 0L
        CloudBackupDownload(
            payload = response.bytes,
            metadata = CloudBackupMetadata(backupId, response.bytes.size.toLong(), checksum, createdAt)
        )
    }

    suspend fun delete(credentials: CloudVaultCredentials): Boolean = withContext(Dispatchers.IO) {
        request(
            method = "DELETE",
            path = "/v1/vaults/${credentials.vaultId}/backup",
            credentials = credentials
        ).code in 200..299
    }

    private fun request(
        method: String,
        path: String,
        credentials: CloudVaultCredentials?,
        body: ByteArray? = null,
        contentType: String? = null,
        extraHeaders: Map<String, String> = emptyMap()
    ): HttpResponse {
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            doInput = true
            useCaches = false
            setRequestProperty("Accept", "application/json")
            // Cloudflare's edge rejects the JVM's default user-agent on some
            // networks. Keep this explicit and stable for Android + Worker
            // observability without sending any user/device identifier.
            setRequestProperty("User-Agent", "StickHub-Android-CloudBackup/1")
            credentials?.let {
                setRequestProperty("X-Vault-Id", it.vaultId)
                setRequestProperty("X-Vault-Secret-Hash", it.secretHashHex)
            }
            contentType?.let { setRequestProperty("Content-Type", it) }
            extraHeaders.forEach { (key, value) -> setRequestProperty(key, value) }
            if (body != null) {
                doOutput = true
                setFixedLengthStreamingMode(body.size)
            }
        }
        try {
            body?.let { connection.outputStream.use { out -> out.write(it) } }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val bytes = stream?.use { it.readBytes() } ?: ByteArray(0)
            if (code !in 200..299) {
                val message = runCatching { JSONObject(bytes.toString(Charsets.UTF_8)).optJSONObject("error")?.optString("message") }
                    .getOrNull()
                    ?.ifBlank { null }
                    ?: "Cloud request failed ($code)"
                throw CloudBackupHttpException(code, message)
            }
            return HttpResponse(
                code = code,
                bytes = bytes,
                backupId = connection.getHeaderField("X-Backup-Id"),
                checksum = connection.getHeaderField("X-Backup-Checksum"),
                createdAt = connection.getHeaderField("X-Backup-Created-At")?.toLongOrNull()
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun parseJson(response: HttpResponse): JSONObject =
        runCatching { JSONObject(response.bytes.toString(Charsets.UTF_8)) }
            .getOrElse { throw IOException("Cloud response is invalid JSON") }

    private data class HttpResponse(
        val code: Int,
        val bytes: ByteArray,
        val backupId: String? = null,
        val checksum: String? = null,
        val createdAt: Long? = null
    )

    companion object {
        const val DEFAULT_BASE_URL = "https://stickhub-cloud.cloud-backup-worker.workers.dev"
        private const val TIMEOUT_MS = 20_000
    }
}
