package com.hkm.stickhub.cloud

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

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
