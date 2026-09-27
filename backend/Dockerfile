# Build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts gradle.lockfile ./
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Run stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# logback-spring.xml writes logs/audit.log relative to /app, so the
# unprivileged user needs a writable logs directory.
RUN adduser -D -g "" appuser && mkdir -p /app/logs && chown appuser /app/logs
USER appuser

COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
