#TODO add mvn
#FROM maven:3.8.5-openjdk-17-slim AS build
#WORKDIR /app
#
#COPY pom.xml /app/pom.xml
#COPY exercises/pom.xml /app/exercises/pom.xml
#COPY exercises/exercise-api /app/exercises/exercise-api
#COPY exercises/exercise-microservice /app/exercises/exercise-microservice
#
#RUN mvn clean package -f /app/exercises/exercise-microservice/pom.xml -DskipTests

FROM openjdk:17-jdk-slim
LABEL authors="sheldor"
RUN mkdir /app
COPY /target/exercise-microservice-0.0.1-SNAPSHOT.jar /app/exercise.jar
WORKDIR /app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "exercise.jar"]
