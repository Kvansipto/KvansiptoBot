FROM openjdk:18-jdk-slim
RUN mkdir /app
COPY /target/exercise-microservice-0.0.1-SNAPSHOT.jar /app/exercise.jar
WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "exercise.jar"]
