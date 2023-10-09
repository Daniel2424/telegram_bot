FROM openjdk:latest

WORKDIR /app

COPY target/springDmBot-0.0.1-SNAPSHOT.jar /app.jar

CMD ["java", "-jar", "/app.jar"]

