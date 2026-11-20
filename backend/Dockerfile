# ---- Build stage: compile the Spring Boot jar ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---- Run stage: small JRE image ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render (and most hosts) pass the port in $PORT; application.properties reads it (default 8080).
EXPOSE 8080
# Keep memory within small free-tier instances (512 MB)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
