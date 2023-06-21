# base image for the docker container
FROM openjdk:18-jdk-slim

# copies the .jar file from the "target" directory into the Docker container
COPY target/*.jar app.jar

# exposes port 8080 in the Docker container, allowing external connections to it
EXPOSE 8080

# sets the command to run when the Docker container starts
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app.jar"]