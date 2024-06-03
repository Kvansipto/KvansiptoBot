FROM openjdk:17-jdk-slim
LABEL authors="sheldor"
RUN mkdir /app
COPY /target/telegram-0.0.1-SNAPSHOT.jar /app/telegram.jar
WORKDIR /app
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "telegram.jar"]