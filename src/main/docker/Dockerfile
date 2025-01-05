FROM maven:3.9.3-openjdk-23 AS build
WORKDIR /app
COPY . .
RUN mvn clean package

FROM openjdk:23-jdk-slim
WORKDIR /app
COPY --from=build /app/src/main/target/Scrabble-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar"]
