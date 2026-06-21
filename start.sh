#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

./scripts/check-ports.sh
set -a
. ./.env.runtime
set +a

docker compose --env-file .env.runtime up --build -d

echo "xgs-12 已启动"
echo "前端访问地址: http://127.0.0.1:${FRONTEND_PORT}"
echo "后端API地址: http://127.0.0.1:${BACKEND_PORT}"
echo "MySQL端口: 127.0.0.1:${MYSQL_PORT}"
echo "Redis端口: 127.0.0.1:${REDIS_PORT}"
