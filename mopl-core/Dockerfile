FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY settings.gradle build.gradle ./
COPY gradle gradle

COPY mopl-core/build.gradle mopl-core/build.gradle

RUN gradle :mopl-core:dependencies --no-daemon

COPY mopl-core/src mopl-core/src

RUN gradle :mopl-core:bootJar --no-daemon

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=builder /app/mopl-core/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
