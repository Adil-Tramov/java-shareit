FROM eclipse-temurin:21-jre-alpine
COPY target/*.jar app.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh
ENTRYPOINT ["./wait-for-it.sh", "db:5432", "-t", "120", "--", "java", "-jar", "app.jar"]