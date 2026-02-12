# Step 1: Build with Maven using Java 21
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Install Maven manually
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Step 2: Run Spring Boot app
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=10000
EXPOSE $PORT
ENTRYPOINT ["java","-jar","app.jar","--server.port=${PORT}"]
