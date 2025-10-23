# Use the Eclipse temurin alpine official image
# https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:21-jdk-alpine

# Create and change to the app directory.
WORKDIR /app

# Copy local code to the container image.
COPY . ./

# Copy Gradle properties for Docker
COPY gradle-docker.properties gradle.properties

# Build the app with Gradle (disable daemon for Docker)
RUN ./gradlew clean build -x test -x check --no-daemon --max-workers=1

# Run the app with Spring Boot fat JAR (exclude plain JAR)
CMD ["sh", "-c", "ls -la build/libs/ && JAR_FILE=$(find build/libs -name 'memocards-*.jar' ! -name '*-plain.jar' | head -1) && echo \"Using JAR: $JAR_FILE\" && java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom -jar \"$JAR_FILE\""]
