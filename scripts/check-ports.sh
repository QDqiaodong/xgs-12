#!/bin/bash

FRONTEND_PORT=8086
BACKEND_PORT=8096
MYSQL_PORT=3312
REDIS_PORT=6385

check_and_adjust_port() {
    local port=$1
    local max_attempts=10
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if ! nc -z localhost $port 2>/dev/null; then
            echo $port
            return 0
        fi
        echo "端口 $port 已被占用，尝试下一个端口..."
        port=$((port + 1))
        attempt=$((attempt + 1))
    done
    
    echo "无法找到可用端口（尝试了 $max_attempts 次）" >&2
    return 1
}

FRONTEND_PORT=$(check_and_adjust_port $FRONTEND_PORT)
BACKEND_PORT=$(check_and_adjust_port $BACKEND_PORT)
MYSQL_PORT=$(check_and_adjust_port $MYSQL_PORT)
REDIS_PORT=$(check_and_adjust_port $REDIS_PORT)

export FRONTEND_PORT
export BACKEND_PORT
export MYSQL_PORT
export REDIS_PORT

echo "最终端口配置："
echo "前端端口: $FRONTEND_PORT"
echo "后端端口: $BACKEND_PORT"
echo "MySQL端口: $MYSQL_PORT"
echo "Redis端口: $REDIS_PORT"