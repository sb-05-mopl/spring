#!/bin/bash

echo "--------------- spring 시작 -----------------"
docker stop spring-websocket || true
docker rm spring-websocket || true

docker pull 989775483620.dkr.ecr.ap-northeast-2.amazonaws.com/mopl/websocket:latest

cd /home/ubuntu/websocket
docker compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.prod.yml up -d --build
echo "--------------- spring 끝 ------------------"