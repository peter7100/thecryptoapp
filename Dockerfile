# ------------------------
# Stage 1: Build the JAR
# ------------------------
FROM maven:3.9.6-eclipse-temurin-11 AS build

WORKDIR /app

# Copy Maven files first (better caching)
COPY pom.xml .
# Optional: pre-download dependencies
# RUN mvn -B -q dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ------------------------
# Stage 2: Runtime image
# ------------------------
FROM eclipse-temurin:11-jre

# Create a non-root user
RUN useradd -r -u 1001 appuser

# Create app and data directories
WORKDIR /app
RUN mkdir -p /app/data \
    && chown -R appuser:appuser /app

# Copy the built JAR
COPY --from=build /app/target/crypto-app-1.0-SNAPSHOT.jar app.jar

# Switch to non-root user
#USER appuser


# Default working directory for input/output files
VOLUME ["/app/data"]
WORKDIR /app/data

# Default command
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
