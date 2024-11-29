# Build stage
FROM maven:3.8.5-jdk-11 AS build
COPY Docker .
RUN mvn clean package -DskipTests

# Package stage
FROM openjdk:11-jdk-slim
COPY --from=build target/Room-1-0.0.1-SNAPSHOT.jar demo.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "demo.jar"]
