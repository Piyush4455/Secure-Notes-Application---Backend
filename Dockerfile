# Step 1: Build the project
FROM maven:3.9.1-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Run the application
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render sets PORT automatically
ENV PORT=10000
EXPOSE $PORT
ENTRYPOINT ["java","-jar","app.jar","--server.port=${PORT}"]
