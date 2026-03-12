# ---------- Stage 1 : Build ----------
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle

COPY src ./src

RUN gradle shadowJar -x test --no-daemon


# ---------- Stage 2 : Runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/student-management-0.1-all.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
