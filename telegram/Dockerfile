#TODO add mvn
#FROM maven:3.8.5-openjdk-17-slim AS build
#WORKDIR /app
#
#COPY src ./telegram/src
#COPY pom.xml ./telegram
#COPY ../pom.xml ./pom.xml
#
## Выполняем команду Maven для сборки проекта
#RUN mvn clean package -f ./telegram/pom.xml -DskipTests

FROM openjdk:17-jdk-slim
LABEL authors="sheldor"
# Установка библиотеки libfreetype
RUN apt-get update && apt-get install -y fontconfig libfreetype6
RUN mkdir /app
COPY /target/telegram-0.0.1-SNAPSHOT.jar /app/telegram.jar
RUN mkdir -p /app/resources
WORKDIR /app
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "telegram.jar"]
