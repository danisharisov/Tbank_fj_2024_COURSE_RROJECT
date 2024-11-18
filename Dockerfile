# Указываем базовый образ для Java
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем jar файл вашего приложения в контейнер
# Предположим, что ваш jar файл называется "app.jar"
COPY build/libs/Tbank_fj_2024_COURSE_RROJECT-0.0.1-SNAPSHOT.jar app.jar

# Устанавливаем переменную окружения для указания, что это Java приложение
ENV SPRING_PROFILES_ACTIVE=prod
# Открываем порт, на котором работает ваше приложение (например, 8080)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
