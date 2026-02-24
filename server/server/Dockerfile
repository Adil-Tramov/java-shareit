FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache netcat-openbsd curl

WORKDIR /app

COPY target/*.jar app.jar

RUN ls -la app.jar

COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

ENTRYPOINT ["/wait-for-it.sh", "db:5432", "-t", "120", "--", "java", "-jar", "app.jar"]