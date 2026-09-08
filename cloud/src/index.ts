import { AwsClient } from "aws4fetch";

interface Env {
  DB: D1Database;
  ALLOWED_ORIGIN: string;
  MAX_BACKUP_BYTES?: string;
  R2_ENDPOINT: string;
  R2_BUCKET: string;
  R2_ACCESS_KEY_ID: string;
  R2_SECRET_ACCESS_KEY: string;
}

const VAULT_ID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const SHA256 = /^[0-9a-f]{64}$/i;
const BACKUP_ID = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
const MAX_DEFAULT_BYTES = 64 * 1024 * 1024;

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
  const maxBytes = Number(env.MAX_BACKUP_BYTES ?? MAX_DEFAULT_BYTES);
  if (!backupId || !BACKUP_ID.test(backupId) || !checksum || !SHA256.test(checksum)) {
    return error("VALIDATION_ERROR", "Backup metadata is invalid.", 400, origin);
  }
  if (!Number.isFinite(contentLength) || contentLength <= 0 || contentLength > maxBytes) {
    return error("BACKUP_TOO_LARGE", "Cloud backup exceeds the size limit.", 413, origin);
  }
  if (!request.body) return error("EMPTY_BACKUP", "Backup payload is empty.", 400, origin);

  const body = await request.arrayBuffer();
  if (body.byteLength !== contentLength) return error("SIZE_MISMATCH", "Backup size mismatch.", 400, origin);
  const digest = await crypto.subtle.digest("SHA-256", body);
  const actual = [...new Uint8Array(digest)].map((b) => b.toString(16).padStart(2, "0")).join("");
  if (!safeEqual(actual, checksum)) return error("CHECKSUM_MISMATCH", "Backup integrity check failed.", 400, origin);

  const key = `stickhub-cloud/${vaultId}/latest.bin`;
  const uploaded = await r2(env).fetch(objectUrl(env, key), {
    method: "PUT",
    body,
    headers: { "content-type": "application/octet-stream", "content-length": String(body.byteLength) },
  });
  if (!uploaded.ok) return error("STORAGE_UNAVAILABLE", "Cloud storage is temporarily unavailable.", 502, origin);

  const now = Date.now();
  await env.DB.prepare(
    `INSERT INTO backups(vault_id, backup_id, object_key, byte_size, checksum, created_at)
     VALUES(?1, ?2, ?3, ?4, ?5, ?6)
     ON CONFLICT(vault_id) DO UPDATE SET backup_id=excluded.backup_id, object_key=excluded.object_key,
       byte_size=excluded.byte_size, checksum=excluded.checksum, created_at=excluded.created_at`
  ).bind(vaultId, backupId, key, body.byteLength, actual, now).run();
  return response({ data: { backupId, byteSize: body.byteLength, checksum: actual, createdAt: now } }, 200, origin);
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
