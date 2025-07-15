# Build stage
FROM gradle:8.7.0-jdk17 AS build

WORKDIR /loganalyzerAdmin
COPY .. .
RUN gradle build --no-daemon -x test

# Run stage
FROM eclipse-temurin:17-jre-alpine

# Copy the certificates into the runtime image
COPY infrastructure/logpulse_server_ca.crt /tmp/logpulse_server_ca.crt

# Add certificate to the JRE truststore
RUN keytool -import \
      -alias logpulse-server-ca \
      -file /tmp/logpulse_server_ca.crt \
      -keystore $JAVA_HOME/lib/security/cacerts \
      -storepass changeit \
      -noprompt && \
    rm /tmp/logpulse_server_ca.crt

# Set working directory
WORKDIR /loganalyzerAdmin

# Copy the built JAR from the build stage
COPY --from=build /loganalyzerAdmin/build/libs/*.jar loganalyzerAdmin.jar

# Create a non-root user
RUN adduser -D appuser && \
    chown -R appuser:appuser /loganalyzerAdmin
USER appuser

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=default

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "loganalyzerAdmin.jar"]