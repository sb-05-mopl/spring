#!/bin/bash

echo "--------------- spring 시작 -----------------"
docker stop spring-app || true
docker rm spring-app || true

docker pull 989775483620.dkr.ecr.ap-northeast-2.amazonaws.com/mopl/spring:latest

cd /home/ubuntu/spring
docker compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.prod.yml up -d --build
echo "--------------- spring 끝 ------------------"