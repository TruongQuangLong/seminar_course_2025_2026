FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY . /workspace

RUN mvn -f /workspace/pom.xml -pl notification-service -am -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/notification-service/target/notification-service-0.0.1-SNAPSHOT.jar /app/notification-service.jar
ENTRYPOINT ["java", "-jar", "/app/notification-service.jar"]
EXPOSE 8084
