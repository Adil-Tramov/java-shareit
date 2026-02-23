FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]