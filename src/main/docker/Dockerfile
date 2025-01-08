FROM maven:3.9.9-amazoncorretto-23 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DmainClass=MainKt

FROM openjdk:23-jdk-slim
WORKDIR /app
COPY --from=build /app/target/Scrabble-1.0-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "app.jar", "--web", "--prod"]
