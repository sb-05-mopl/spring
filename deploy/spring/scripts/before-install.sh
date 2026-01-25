#!/bin/bash
if [ -f /home/ubuntu/spring/.env ]; then
    cp /home/ubuntu/spring/.env /tmp/.env.backup
fi

rm -rf /home/ubuntu/spring/*
rm -rf /home/ubuntu/spring/.*  2>/dev/null || true

if [ -f /tmp/.env.backup ]; then
    mkdir -p /home/ubuntu/spring
    mv /tmp/.env.backup /home/ubuntu/spring/.env
fi
