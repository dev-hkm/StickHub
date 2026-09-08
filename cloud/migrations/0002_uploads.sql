CREATE TABLE IF NOT EXISTS uploads (
 vault_id TEXT NOT NULL,
 backup_id TEXT NOT NULL,
 upload_id TEXT NOT NULL,
 object_key TEXT NOT NULL,
 byte_size INTEGER NOT NULL,
 checksum TEXT NOT NULL,
 part_size INTEGER NOT NULL,
 created_at INTEGER NOT NULL,
 PRIMARY KEY(vault_id, backup_id)
);
