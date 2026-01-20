#!/bin/bash

echo "--------------- nginx 시작 -----------------"
docker stop nginx || true
docker rm nginx || true
docker pull 989775483620.dkr.ecr.ap-northeast-2.amazonaws.com/mopl/nginx:latest

cd /home/ubuntu/nginx
docker compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.prod.yml up -d --build
echo "--------------- nginx 끝 ------------------"