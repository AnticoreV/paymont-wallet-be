FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle

RUN gradle dependencies --no-daemon || true

COPY src src

RUN gradle clean bootJar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
