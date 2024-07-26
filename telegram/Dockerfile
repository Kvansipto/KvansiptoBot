FROM openjdk:18-jdk-slim
LABEL authors="sheldor"
# Установка библиотеки libfreetype
RUN apt-get update && apt-get install -y fontconfig libfreetype6
RUN mkdir /app
COPY /target/telegram-0.0.1-SNAPSHOT.jar /app/telegram.jar
RUN mkdir -p /app/resources
WORKDIR /app
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "telegram.jar"]
