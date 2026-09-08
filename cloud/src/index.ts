import { AwsClient } from "aws4fetch";

interface Env {
  DB: D1Database;
  ALLOWED_ORIGIN: string;
  R2_ENDPOINT: string;
  R2_BUCKET: string;
  R2_ACCESS_KEY_ID: string;
  R2_SECRET_ACCESS_KEY: string;
}

const VAULT_ID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const SHA256 = /^[0-9a-f]{64}$/i;
const BACKUP_ID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
// R2 multipart object limit, not an application subscription quota.
const R2_MAX_BYTES = 5 * 1024 ** 4;

const jsonHeaders = {
  "content-type": "application/json; charset=utf-8",
  "cache-control": "no-store",
};

function response(body: unknown, status = 200, origin = "*"): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...jsonHeaders,
      "access-control-allow-origin": origin,
      "access-control-allow-headers": "content-type,x-vault-id,x-vault-secret-hash,x-backup-id,x-backup-checksum",
      "access-control-allow-methods": "GET,POST,PUT,DELETE,OPTIONS",
      "vary": "Origin",
    },
  });
}

function error(code: string, message: string, status: number, origin: string): Response {
  return response({ error: { code, message } }, status, origin);
}

function originFor(request: Request, env: Env): string {
  const origin = request.headers.get("origin");
  return origin && (origin === env.ALLOWED_ORIGIN || origin === "http://localhost:5173")
    ? origin
    : env.ALLOWED_ORIGIN;
}

function isUuid(value: string | null | undefined): value is string {
  return Boolean(value && VAULT_ID.test(value));
}

function safeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let result = 0;
  for (let i = 0; i < a.length; i += 1) result |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return result === 0;
}

function r2(env: Env): AwsClient {
  return new AwsClient({
    accessKeyId: env.R2_ACCESS_KEY_ID,
    secretAccessKey: env.R2_SECRET_ACCESS_KEY,
    service: "s3",
    region: "auto",
  });
}

function objectUrl(env: Env, key: string): string {
  const encoded = key.split("/").map(encodeURIComponent).join("/");
  return `${env.R2_ENDPOINT.replace(/\/$/, "")}/${encodeURIComponent(env.R2_BUCKET)}/${encoded}`;
}

async function authenticatedVault(request: Request, env: Env, vaultId: string): Promise<Response | null> {
  if (!isUuid(vaultId)) return error("INVALID_VAULT", "Invalid vault id.", 400, originFor(request, env));
  const proof = request.headers.get("x-vault-secret-hash")?.trim().toLowerCase();
  if (!proof || !SHA256.test(proof)) return error("UNAUTHORIZED", "Missing vault proof.", 401, originFor(request, env));

  const row = await env.DB.prepare(
    "SELECT secret_hash FROM vaults WHERE vault_id = ?1"
  ).bind(vaultId).first<{ secret_hash: string }>();
  if (!row || !safeEqual(row.secret_hash, proof)) {
    return error("UNAUTHORIZED", "Vault proof is invalid.", 401, originFor(request, env));
  }
  await env.DB.prepare("UPDATE vaults SET last_seen_at = ?1 WHERE vault_id = ?2")
    .bind(Date.now(), vaultId).run();
  return null;
}

async function registerVault(request: Request, env: Env): Promise<Response> {
  const origin = originFor(request, env);
  let body: { vaultId?: string; secretHash?: string };
  try {
    body = await request.json();
  } catch {
    return error("INVALID_JSON", "Request body is invalid JSON.", 400, origin);
  }
  const vaultId = body.vaultId?.trim();
  const secretHash = body.secretHash?.trim().toLowerCase();
  if (!isUuid(vaultId) || !secretHash || !SHA256.test(secretHash)) {
    return error("VALIDATION_ERROR", "Vault credentials are invalid.", 400, origin);
  }

  const now = Date.now();
  const existing = await env.DB.prepare(
    "SELECT secret_hash FROM vaults WHERE vault_id = ?1"
  ).bind(vaultId).first<{ secret_hash: string }>();
  if (existing && !safeEqual(existing.secret_hash, secretHash)) {
    return error("VAULT_CONFLICT", "Vault id is already registered.", 409, origin);
  }
  if (!existing) {
    await env.DB.prepare(
      "INSERT INTO vaults(vault_id, secret_hash, created_at, last_seen_at) VALUES(?1, ?2, ?3, ?3)"
    ).bind(vaultId, secretHash, now).run();
  } else {
    await env.DB.prepare("UPDATE vaults SET last_seen_at = ?1 WHERE vault_id = ?2")
      .bind(now, vaultId).run();
  }
  return response({ data: { vaultId, registered: true } }, existing ? 200 : 201, origin);
}

async function uploadBackup(request: Request, env: Env, vaultId: string): Promise<Response> {
  const origin = originFor(request, env);
  const auth = await authenticatedVault(request, env, vaultId);
  if (auth) return auth;

  const backupId = request.headers.get("x-backup-id")?.trim();
  const checksum = request.headers.get("x-backup-checksum")?.trim().toLowerCase();
  const contentLength = Number(request.headers.get("content-length") ?? "0");
  const maxBytes = R2_MAX_BYTES;
  if (!backupId || !BACKUP_ID.test(backupId) || !checksum || !SHA256.test(checksum)) {
    return error("VALIDATION_ERROR", "Backup metadata is invalid.", 400, origin);
  }
  if (!Number.isFinite(contentLength) || contentLength <= 0 || contentLength > maxBytes) {
    return error("BACKUP_TOO_LARGE", "Cloud backup exceeds the size limit.", 413, origin);
  }
  if (!request.body) return error("EMPTY_BACKUP", "Backup payload is empty.", 400, origin);

  // Old clients must upgrade; never buffer an arbitrary backup in Worker RAM.
  return error("UPGRADE_REQUIRED", "Update StickHub to use large-file cloud backup.", 426, origin);
}

type Upload = { upload_id: string; object_key: string; byte_size: number; checksum: string; part_size: number; created_at: number };
const xmlEscape = (s: string) => s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
const xmlValue = (s: string, name: string) => s.match(new RegExp(`<${name}>([^<]+)</${name}>`))?.[1]
  .replace(/&quot;/g, '"').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&amp;/g, '&');

async function multipart(request: Request, env: Env, vaultId: string, backupId: string, action: string): Promise<Response> {
  const origin = originFor(request, env);
  const auth = await authenticatedVault(request, env, vaultId);
  if (auth) return auth;
  if (!BACKUP_ID.test(backupId)) return error("INVALID_ID", "Invalid backup id.", 400, origin);
  const aws = r2(env);
  let row = await env.DB.prepare("SELECT * FROM uploads WHERE vault_id=?1 AND backup_id=?2").bind(vaultId, backupId).first<Upload>();
  if (action === "init" && request.method === "POST") {
    const input = await request.json<{ byteSize: number; checksum: string }>();
    if (!Number.isSafeInteger(input.byteSize) || input.byteSize <= 0 || input.byteSize > R2_MAX_BYTES || !SHA256.test(input.checksum)) {
      return error("INVALID_SIZE", "Invalid backup size or checksum (R2 maximum: 5 TiB).", 400, origin);
    }
    if (row && (row.byte_size !== input.byteSize || row.checksum !== input.checksum)) return error("CONFLICT", "Backup id conflict.", 409, origin);
    if (!row) {
      const key = `stickhub-cloud/${vaultId}/${backupId}.bin`;
      const started = await aws.fetch(objectUrl(env, key) + "?uploads", { method: "POST", headers: { "content-type": "application/octet-stream", "x-amz-meta-sha256": input.checksum } });
      const uploadId = xmlValue(await started.text(), "UploadId");
      if (!started.ok || !uploadId) return error("STORAGE", "Couldn't start R2 upload.", 502, origin);
      // At most 10,000 R2 parts; use larger parts for very large files.
      const partSize = Math.max(8 * 1024 * 1024, Math.ceil(input.byteSize / 10000 / (1024 * 1024)) * 1024 * 1024);
      row = { upload_id: uploadId, object_key: key, byte_size: input.byteSize, checksum: input.checksum, part_size: partSize, created_at: Date.now() };
      await env.DB.prepare("INSERT INTO uploads VALUES(?1,?2,?3,?4,?5,?6,?7,?8)").bind(vaultId, backupId, uploadId, key, input.byteSize, input.checksum, partSize, row.created_at).run();
    }
    return response({ data: { partSize: row.part_size } }, 200, origin);
  }
  if (!row) return error("NOT_FOUND", "Upload session not found.", 404, origin);
  const uploadUrl = new URL(objectUrl(env, row.object_key));
  uploadUrl.searchParams.set("uploadId", row.upload_id);
  if (action === "part" && request.method === "POST") {
    const { partNumber } = await request.json<{ partNumber: number }>();
    if (!Number.isInteger(partNumber) || partNumber < 1 || partNumber > Math.ceil(row.byte_size / row.part_size)) return error("INVALID_PART", "Invalid part number.", 400, origin);
    uploadUrl.searchParams.set("partNumber", String(partNumber));
    uploadUrl.searchParams.set("X-Amz-Expires", "3600");
    const signed = await aws.sign(uploadUrl.toString(), { method: "PUT", aws: { signQuery: true } });
    return response({ data: { url: signed.url } }, 200, origin);
  }
  if (action === "abort" && request.method === "POST") {
    await aws.fetch(uploadUrl, { method: "DELETE" });
    await env.DB.prepare("DELETE FROM uploads WHERE vault_id=?1 AND backup_id=?2").bind(vaultId, backupId).run();
    return response({ data: { aborted: true } }, 200, origin);
  }
  if (action === "complete" && request.method === "POST") {
    const { parts } = await request.json<{ parts: { partNumber: number; etag: string }[] }>();
    if (!Array.isArray(parts) || parts.length !== Math.ceil(row.byte_size / row.part_size) || parts.some((p, i) => p.partNumber !== i + 1 || typeof p.etag !== "string" || !/^"?[a-f0-9]{32}"?$/i.test(p.etag))) return error("INVALID_PARTS", "Incomplete upload parts.", 400, origin);
    // HEAD makes completion retryable if the response was lost after R2 committed.
    let head = await aws.fetch(objectUrl(env, row.object_key), { method: "HEAD" });
    if (!head.ok) {
      const xml = `<CompleteMultipartUpload>${parts.map(p => `<Part><PartNumber>${p.partNumber}</PartNumber><ETag>${xmlEscape(p.etag)}</ETag></Part>`).join("")}</CompleteMultipartUpload>`;
      const completed = await aws.fetch(uploadUrl, { method: "POST", body: xml, headers: { "content-type": "application/xml" } });
      const result = await completed.text();
      if (!completed.ok || result.includes("<Error>") || !result.includes("CompleteMultipartUploadResult")) return error("STORAGE", "R2 couldn't complete the backup. Retry upload.", 502, origin);
      head = await aws.fetch(objectUrl(env, row.object_key), { method: "HEAD" });
    }
    if (!head.ok || Number(head.headers.get("content-length")) !== row.byte_size || head.headers.get("x-amz-meta-sha256") !== row.checksum) return error("INTEGRITY", "Uploaded backup verification failed.", 502, origin);
    // Publish only the completed immutable object; the previous snapshot survives failures.
    await env.DB.prepare(`INSERT INTO backups(vault_id,backup_id,object_key,byte_size,checksum,created_at) VALUES(?1,?2,?3,?4,?5,?6)
      ON CONFLICT(vault_id) DO UPDATE SET backup_id=excluded.backup_id,object_key=excluded.object_key,byte_size=excluded.byte_size,checksum=excluded.checksum,created_at=excluded.created_at
      WHERE excluded.created_at >= backups.created_at`).bind(vaultId, backupId, row.object_key, row.byte_size, row.checksum, row.created_at).run();
    return response({ data: { backupId, byteSize: row.byte_size, checksum: row.checksum, createdAt: row.created_at } }, 200, origin);
  }
  return error("NOT_FOUND", "Route not found.", 404, origin);
}

async function downloadBackup(request: Request, env: Env, vaultId: string): Promise<Response> {
  const origin = originFor(request, env);
  const auth = await authenticatedVault(request, env, vaultId);
  if (auth) return auth;
  const row = await env.DB.prepare(
    "SELECT backup_id, object_key, byte_size, checksum, created_at FROM backups WHERE vault_id = ?1"
  ).bind(vaultId).first<{ backup_id: string; object_key: string; byte_size: number; checksum: string; created_at: number }>();
  if (!row) return error("NOT_FOUND", "No cloud backup exists yet.", 404, origin);
  const stored = await r2(env).fetch(objectUrl(env, row.object_key), { method: "GET" });
  if (!stored.ok || !stored.body) return error("STORAGE_UNAVAILABLE", "Cloud backup could not be read.", 502, origin);
  return new Response(stored.body, {
    status: 200,
    headers: {
      "content-type": "application/octet-stream",
      "content-length": String(row.byte_size),
      "x-backup-id": row.backup_id,
      "x-backup-checksum": row.checksum,
      "x-backup-created-at": String(row.created_at),
      "cache-control": "no-store",
      "access-control-allow-origin": origin,
      "access-control-expose-headers": "x-backup-id,x-backup-checksum,x-backup-created-at",
      "vary": "Origin",
    },
  });
}

async function deleteBackup(request: Request, env: Env, vaultId: string): Promise<Response> {
  const origin = originFor(request, env);
  const auth = await authenticatedVault(request, env, vaultId);
  if (auth) return auth;
  const row = await env.DB.prepare("SELECT object_key FROM backups WHERE vault_id = ?1")
    .bind(vaultId).first<{ object_key: string }>();
  if (row) await r2(env).fetch(objectUrl(env, row.object_key), { method: "DELETE" });
  await env.DB.prepare("DELETE FROM backups WHERE vault_id = ?1").bind(vaultId).run();
  return response({ data: { deleted: true } }, 200, origin);
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const origin = originFor(request, env);
    if (request.method === "OPTIONS") return response({}, 204, origin);
    const url = new URL(request.url);
    try {
      if (request.method === "GET" && url.pathname === "/health") {
        return response({ data: { status: "ok", service: "stickhub-cloud" } }, 200, origin);
      }
      if (request.method === "POST" && url.pathname === "/v1/vaults") {
        return registerVault(request, env);
      }
      const uploadMatch = url.pathname.match(/^\/v2\/vaults\/([^/]+)\/uploads\/([^/]+)\/(init|part|complete|abort)$/);
      if (uploadMatch) return await multipart(request, env, uploadMatch[1], uploadMatch[2], uploadMatch[3]);
      const match = url.pathname.match(/^\/v1\/vaults\/([^/]+)\/backup$/);
      if (match) {
        const vaultId = match[1];
        if (request.method === "PUT") return uploadBackup(request, env, vaultId);
        if (request.method === "GET") return downloadBackup(request, env, vaultId);
        if (request.method === "DELETE") return deleteBackup(request, env, vaultId);
      }
      return error("NOT_FOUND", "Route not found.", 404, origin);
    } catch (cause) {
      console.error(JSON.stringify({ event: "request_failed", path: url.pathname, cause: String(cause) }));
      return error("INTERNAL_ERROR", "Unexpected cloud service error.", 500, origin);
    }
  },
};
