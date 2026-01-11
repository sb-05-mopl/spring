FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY settings.gradle build.gradle ./
COPY gradle gradle

COPY mopl-websocket-sse/build.gradle mopl-websocket-sse/build.gradle

RUN gradle :mopl-websocket-sse:dependencies --no-daemon

COPY mopl-websocket-sse/src mopl-websocket-sse/src
RUN gradle :mopl-websocket-sse:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=builder /app/mopl-websocket-sse/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
