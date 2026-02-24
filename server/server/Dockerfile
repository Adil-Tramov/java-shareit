FROM eclipse-temurin:21-jre-alpine
COPY target/*.jar app.jar
RUN echo "JAR file size:" && ls -la app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]