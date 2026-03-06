#!/bin/bash

set -e  # Exit on error

echo "🚀 FinSync Deployment Setup"
echo "=============================="

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Variables
REPO_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
NGINX_CONF="$REPO_ROOT/nginx.conf"
GITHUB_RAW="https://raw.githubusercontent.com/RicardoNoyaCamacho/core-service/main/nginx.conf"

echo -e "${YELLOW}1. Verificando directorio...${NC}"
if [ ! -f "$REPO_ROOT/docker-compose.yml" ]; then
    echo -e "${RED}❌ No se encontró docker-compose.yml${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Directorio correcto${NC}"

echo -e "${YELLOW}2. Descargando nginx.conf desde GitHub...${NC}"
if command -v curl &> /dev/null; then
    curl -fsSL "$GITHUB_RAW" -o "$NGINX_CONF"
    echo -e "${GREEN}✅ Archivo descargado${NC}"
else
    echo -e "${RED}❌ curl no instalado${NC}"
    exit 1
fi

echo -e "${YELLOW}3. Validando sintaxis de nginx.conf...${NC}"
if docker run --rm -v "$NGINX_CONF:/etc/nginx/nginx.conf:ro" nginx:alpine nginx -t 2>&1 | grep -q "successful"; then
    echo -e "${GREEN}✅ Sintaxis válida${NC}"
else
    echo -e "${RED}❌ Error en sintaxis${NC}"
    exit 1
fi

echo -e "${YELLOW}4. Deteniendo servicios previos...${NC}"
cd "$REPO_ROOT"
docker-compose down
echo -e "${GREEN}✅ Servicios detenidos${NC}"

echo -e "${YELLOW}5. Reconstruyendo Docker Compose...${NC}"
docker-compose -f docker-compose.yml --env-file .env.production up -d --build
echo -e "${GREEN}✅ Servicios iniciados${NC}"

echo -e "${YELLOW}6. Esperando que los servicios se estabilicen...${NC}"
sleep 15

echo -e "${YELLOW}7. Verificando estado...${NC}"
if docker-compose ps | grep -q "finsync-nginx.*Up"; then
    echo -e "${GREEN}✅ Nginx está corriendo${NC}"
else
    echo -e "${RED}❌ Nginx no está corriendo${NC}"
    docker-compose logs nginx | tail -20
    exit 1
fi

echo ""
echo -e "${GREEN}════════════════════════════════════${NC}"
echo -e "${GREEN}✅ DEPLOYMENT COMPLETADO CON ÉXITO${NC}"
echo -e "${GREEN}════════════════════════════════════${NC}"
echo ""
echo "Frontend:  http://140.84.187.207/"
echo "Backend:   http://140.84.187.207:8080/api/v1"
echo ""
echo "Ver logs: docker-compose logs -f nginx"