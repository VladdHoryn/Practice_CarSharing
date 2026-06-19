FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY /target/analytics-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
