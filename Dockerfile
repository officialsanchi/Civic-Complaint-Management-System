# --- Stage 1: Build the application ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application (skipping tests to speed up the build)
# Package the application (skipping test compilation and execution)
RUN mvn clean package -Dmaven.test.skip=true

# --- Stage 2: Run the application ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port your Spring Boot app runs on (usually 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]