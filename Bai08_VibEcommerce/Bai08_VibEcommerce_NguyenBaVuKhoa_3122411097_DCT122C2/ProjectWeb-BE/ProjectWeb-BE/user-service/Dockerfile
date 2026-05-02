# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy parent pom (needed for child module resolution)
COPY pom.xml /workspace/pom.xml

# Copy this service's pom and source
COPY user-service/pom.xml /workspace/user-service/pom.xml
COPY user-service/src /workspace/user-service/src

# Install parent pom first, then build the service
RUN mvn -f /workspace/pom.xml install -N -DskipTests && \
    mvn -f /workspace/user-service/pom.xml clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/user-service/target/user-service-0.0.1-SNAPSHOT-exec.jar /app/user-service.jar
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]
EXPOSE 8081
