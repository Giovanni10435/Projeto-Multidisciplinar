#!/bin/bash

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "======================================"
echo "       Iniciando AcolheSP"
echo "======================================"

# PostgreSQL
echo "[1/4] Iniciando PostgreSQL..."
cd "$ROOT_DIR"
docker compose up -d

# Backend
echo "[2/4] Abrindo terminal do Backend..."
wt.exe new-tab --title "AcolheSP - Backend" wsl.exe bash -c "cd '$ROOT_DIR/backend/demo' && ./mvnw spring-boot:run; exec bash"

# BFF
echo "[3/4] Abrindo terminal do BFF..."
wt.exe new-tab --title "AcolheSP - BFF" wsl.exe bash -c "cd '$ROOT_DIR/BFF/bff' && ./mvnw spring-boot:run; exec bash"

# Frontend
echo "[4/4] Abrindo terminal do Frontend..."
wt.exe new-tab --title "AcolheSP - Frontend" wsl.exe bash -c "cd '$ROOT_DIR/frontend' && npm run dev; exec bash"

echo ""
echo "AcolheSP iniciado!"
echo ""
echo "Frontend: http://localhost:5173"
echo "BFF:      http://localhost:8081"
echo "Backend:  http://localhost:8080"