FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/booking-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
