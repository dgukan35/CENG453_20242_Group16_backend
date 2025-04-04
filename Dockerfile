FROM maven:3-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src /app/src

RUN mvn test

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-alpine
WORKDIR /app

COPY --from=build /app/target/uno-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=render", "-jar", "app.jar"]
