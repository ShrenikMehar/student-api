# ---------- Stage 1 : Build ----------
FROM gradle:8.7-jdk21 AS builder

WORKDIR /app

COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Warm the Gradle dependency cache before copying application sources.
RUN ./gradlew dependencies --no-daemon >/dev/null

COPY src ./src

RUN ./gradlew shadowJar -x test --no-daemon


# ---------- Stage 2 : Runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/student-management-0.1-all.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
