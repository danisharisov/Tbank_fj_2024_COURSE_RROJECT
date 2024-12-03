FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/Tbank_fj_2024_COURSE_RROJECT-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
