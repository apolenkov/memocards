# Use the Eclipse temurin alpine official image
# https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:21-jdk-alpine

# Create and change to the app directory.
WORKDIR /app

# Copy local code to the container image.
COPY . ./

# Build the app with Gradle
RUN ./gradlew clean build -x test

# Run the app by dynamically finding the JAR file in the build/libs directory
CMD ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -jar build/libs/*.jar"]
