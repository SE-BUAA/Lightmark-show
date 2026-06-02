#!/usr/bin/env bash
set -euo pipefail

# One-click deploy on server
# Usage:
#   bash deploy-server.sh [archive_name] [target_dir] [archive_dir]
# Example:
#   bash deploy-server.sh timemark.tar.gz /home/ubuntu/timemark /home/ubuntu

ARCHIVE_NAME="${1:-timemark.tar.gz}"
TARGET_DIR="${2:-/home/ubuntu/timemark}"
ARCHIVE_DIR="${3:-/home/ubuntu}"
ARCHIVE_PATH="${ARCHIVE_DIR}/${ARCHIVE_NAME}"

if ! command -v docker >/dev/null 2>&1; then
  echo "[ERROR] docker is not installed"
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "[ERROR] docker compose plugin is not available"
  exit 1
fi

if [[ ! -f "$ARCHIVE_PATH" ]]; then
  echo "[ERROR] Archive not found: $ARCHIVE_PATH"
  echo "        Please upload it first, e.g.: scp timemark.tar.gz user@server:${ARCHIVE_DIR}/"
  exit 1
fi

echo "[1/5] Preparing target directory: $TARGET_DIR"
mkdir -p "$TARGET_DIR"

echo "[2/5] Cleaning old files (keeping .env if exists)"
if [[ -f "$TARGET_DIR/.env" ]]; then
  find "$TARGET_DIR" -mindepth 1 -maxdepth 1 ! -name '.env' -exec rm -rf {} +
else
  rm -rf "$TARGET_DIR"/*
fi

echo "[3/5] Extracting archive: $ARCHIVE_PATH"
# Archive from package-project.ps1 contains project contents at root level
tar -xzf "$ARCHIVE_PATH" -C "$TARGET_DIR"

echo "[4/5] Starting containers"
cd "$TARGET_DIR"
docker compose up -d --build

echo "[5/5] Done"
docker compose ps

echo "\nLogs (optional):"
echo "  docker compose logs -f backend"
echo "  docker compose logs -f nginx"
