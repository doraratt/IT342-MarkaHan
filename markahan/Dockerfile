# Build stage
FROM maven:3.9.9-openjdk-17 AS build
WORKDIR /app
COPY markahan/pom.xml .
COPY markahan/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/markahan-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]