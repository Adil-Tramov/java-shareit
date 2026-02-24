FROM eclipse-temurin:21-jre-jammy
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=docker"]