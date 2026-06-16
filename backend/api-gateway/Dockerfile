FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY /target/api-gateway-1.0-SNAPSHOT.jar app.jar

EXPOSE 8100

ENTRYPOINT ["java", "-jar", "app.jar"]
