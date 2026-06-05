# ─── Stage 1: Build ───────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first — Maven deps cached unless pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build JAR
COPY src ./src
RUN mvn package -DskipTests -B

# ─── Stage 2: Run ─────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]