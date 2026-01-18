# ------------------------
# Stage 1: Build & verify
# ------------------------
FROM maven:3.9.2-jdk-17 AS build

WORKDIR /app

# Copy project files
COPY pom.xml .
COPY src ./src

# Download OpenJML
RUN curl -L -o openjml.jar https://github.com/OpenJML/OpenJML/releases/download/v21/openjml-21.jar

# Build the project with Maven
RUN mvn clean package -DskipTests

# Run OpenJML ESC verification (core methods only)
RUN java -jar openjml.jar --esc \
      -classpath "target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=cp.txt && cat cp.txt)" \
      --method encrypt,decrypt,process \
      src/main/java/com/example/crypto/FileCryptoApp.java

# ------------------------
# Stage 2: Runtime image
# ------------------------
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the compiled JAR from the build stage
COPY --from=build /app/target/crypto-app-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
