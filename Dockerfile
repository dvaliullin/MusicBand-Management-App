FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml ./common/
COPY client/pom.xml ./client/
COPY server/pom.xml ./server/

RUN mvn dependency:go-offline -B

COPY common ./common
COPY server ./server

RUN mvn clean package -pl server -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/server/target/server.jar /app/server.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/server.jar"]
