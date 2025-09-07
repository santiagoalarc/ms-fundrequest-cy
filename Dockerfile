FROM openjdk:21-jdk-slim

LABEL authors="santiagoalarcon"

WORKDIR /app

COPY applications/app-service/build/libs/MsFundRequest.jar /app/app.jar

COPY .env /app/.env

EXPOSE 8080

ARG SPRING_PROFILES_ACTIVE

ENTRYPOINT ["java", "-jar", "app.jar"]
