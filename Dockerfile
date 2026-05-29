FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

# Run the application directly from the target folder
ENTRYPOINT ["java", "-jar", "target/cic-0.0.1-SNAPSHOT.jar"]
