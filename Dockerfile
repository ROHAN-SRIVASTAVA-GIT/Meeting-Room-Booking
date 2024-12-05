# Base image with OpenJDK 22
FROM openjdk:22-jdk-slim AS build

# Install Maven
RUN apt-get update && apt-get install -y maven && apt-get clean

# Set the working directory
WORKDIR /Meeting-Room-Booking                                                                                                                                                                                                                                                                    
  
  
  
  
  
  
  
  
  
  
  
  
  

# Copy the pom.xml and the source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Package stage
FROM openjdk:22-jdk-slim

# Set the working directory for the final image
WORKDIR /Meeting-Room-Booking                                        
  
  

# Copy the JAR file from the build stage
COPY --from=build /app/target/Room-1-0.0.1-SNAPSHOT.jar ./Room.jar

# Expose the application port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "Room.jar"]
