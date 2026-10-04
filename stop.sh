#!/bin/bash

echo "======================================"
echo "       Encerrando AcolheSP"
echo "======================================"

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo ""
echo "[1/4] Parando Frontend..."

pkill -f "$ROOT_DIR/frontend/node_modules/.bin/vite" 2>/dev/null

echo "[2/4] Parando BFF..."

pkill -f "$ROOT_DIR/BFF/bff" 2>/dev/null

echo "[3/4] Parando Backend..."

pkill -f "$ROOT_DIR/backend/demo" 2>/dev/null

echo "[4/4] Parando PostgreSQL..."

cd "$ROOT_DIR"
docker compose down

echo ""
echo "======================================"
echo "       AcolheSP encerrado!"
echo "======================================"