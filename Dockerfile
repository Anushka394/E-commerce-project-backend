# Stage 1 — build the jar
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 — run the jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/ecommerce-api-1.0.0.jar app.jar
EXPOSE 8080

# Use shell form (not exec form) so $PORT env variable is resolved correctly
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
