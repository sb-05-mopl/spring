#!/bin/bash

cd /home/ubuntu/spring

echo "==배포시작=="

docker pull 989775483620.dkr.ecr.ap-northeast-2.amazonaws.com/mopl/spring:latest

CURRENT=$(cat .current_deployment 2>/dev/null || echo "none")
[ "$CURRENT" == "blue" ] && TARGET="green" || TARGET="blue"

echo "배포: $CURRENT → $TARGET"

docker compose -f docker-compose.prod.yml up -d spring-$TARGET
sleep 60

[ "$CURRENT" != "none" ] && docker compose -f docker-compose.prod.yml rm -sf spring-$CURRENT

echo $TARGET > .current_deployment
docker image prune -f

echo "완료: spring-$TARGET"
