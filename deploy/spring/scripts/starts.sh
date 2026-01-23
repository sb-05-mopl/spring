#!/bin/bash

cd /home/ubuntu/spring

echo "==배포시작=="

docker pull 989775483620.dkr.ecr.ap-northeast-2.amazonaws.com/mopl/spring:latest

docker compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
docker compose -f docker-compose.prod.yml up -d --build

docker image prune -f

echo "완료: spring"
