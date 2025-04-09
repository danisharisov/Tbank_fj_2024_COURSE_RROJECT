# PickAFlick: Сервис выбора фильмов

## 📜 Обзор
**PickAFlick** — это Telegram-бот для совместного выбора и оценки фильмов. Сервис позволяет пользователям оценивать фильмы, обмениваться оценками и выбирать, что посмотреть вместе с друзьями. Для получения данных о фильмах используется OMDb API.

---

## 🚀 Основной функционал
- **Интерактивный Telegram-бот**: Обеспечивает удобный интерфейс для взаимодействия.
- **Оценка фильмов**: Пользователи могут оценивать фильмы и просматривать средние оценки своих друзей.
- **Управление друзьями**: Добавление, удаление друзей и просмотр списка.
- **Списки фильмов**: Списки запланированных и просмотренных фильмов.
- **Интеграция с OMDb API**: Данные о фильмах (постеры, описания, оценки) загружаются автоматически.
- **Безопасность**: HTTPS для взаимодействия с Telegram Webhook.

---

## 🛠 Технологический стек
- **Язык программирования**: Java (Spring Boot)
- **База данных**: PostgreSQL
- **Очередь сообщений**: RabbitMQ
- **Веб-сервер**: Nginx
- **Контейнеризация**: Docker, Docker Compose
- **Внешние API**: OMDb API
- **SSL-сертификаты**: Let’s Encrypt

---

## ⚙ Архитектура
1. **Telegram-бот**:
    - Взаимодействует с пользователями, обрабатывая команды и события через Webhook.
    - Отправляет запросы к бэкенду для управления действиями.
2. **Бэкенд**:
    - Обрабатывает сообщения, отправленные через RabbitMQ.
    - Обрабатывает запросы и хранит данные в базе.
3. **База данных**:
    - PostgreSQL используется для хранения информации о пользователях, фильмах, оценках и друзьях.
4. **Очередь сообщений**:
    - RabbitMQ обрабатывает запросы в порядке очереди.
5. **Nginx**:
    - Реверс-прокси для маршрутизации запросов, поддержка HTTPS через SSL-сертификаты.
6. **OMDb API**:
    - Источник данных о фильмах (названия, постеры, рейтинги).

![Диаграмма компонентов](https://github.com/user-attachments/assets/a3ef89e5-6d26-4017-9d6f-7b9b11c3d3d0)


---

## 📚 Стуктура БД

![DB](https://github.com/user-attachments/assets/df17e00f-660a-45d0-a910-30008585cd37)


## 🌐 Деплой
Проект развёрнут на удаленном сервере в контейнерах с использованием Docker. Nginx используется в качестве реверс-прокси, обеспечивая поддержку HTTPS через Let’s Encrypt. Webhook URL Telegram-бота настроен на HTTPS.

## Инструкция по запуску

### Описание
Бот поддерживает два режима запуска: 
1. **`local_dev`** — используется TelegramLongPollingBot.
2. **`review_dev_to_webhook`** — используется Telegram Webhook.

---

## Требования
Перед запуском убедитесь, что у вас установлены следующие компоненты:
- **Java 17+**
- **PostgreSQL 14+**
- **Docker и Docker Compose** (для ветки `review_dev_to_webhook`)
- **RabbitMQ** (либо в Docker, либо локально)
- **nginx** (для ветки с вебхуками)

---

## 🚀 Запуск ветки `local_dev`

### ⚙ Настройка `application.properties`
Откройте файл `src/main/resources/application.properties` и измените переменные:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/{ВАШЕ_ИМЯ_БД}
spring.datasource.username={ВАШ_ПОСТГРЕС_ЛОГИН}
spring.datasource.password={ВАШ_ПОСТГРЕС_ПАРОЛЬ}
telegram.bot.token={ВАШ_ТОКЕН_БОТА}
omdb.api.key={ВАШ_КЛЮЧ_OMDB_API}
```

1. **Создайте базу данных в PostgreSQL**
```sql
CREATE DATABASE movie_ratings_db;
```
2. **Запустите RabbitMQ**
```bash
docker run -d --name rabbitmq -p 5672:5672 rabbitmq
```
3. **Запустите проект**

## 🚀 Запуск ветки `review_dev_to_webhook` (or main)
1. Создайте файл .env в корне проекта
```.env
POSTGRES_DB=movie_ratings_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=admin

RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672

SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/movie_ratings_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=admin

TELEGRAM_BOT_TOKEN={ВАШ_ТОКЕН_БОТА_ТЕЛЕГРАМ}
TELEGRAM_BOT_USERNAME={ВАШ_ЮЗЕРНЕЙМ_БОТА}
WEBHOOK_URL=https://{ВАШ_ДОМЕН}/webhook
WEBHOOK_SECRET_TOKEN={ВАШ_СЕКРЕТНЫЙ_КЛЮЧ}

OMDB_API_KEY={ВАШ_КЛЮЧ_OMDB_API}
OMDB_API_URL=http://www.omdbapi.com
```
2. Убедитесь, что nginx настроен для работы с Telegram Webhook
```default.conf
server {
    listen 80;
    server_name {ВАШ_ДОМЕН};
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl;
    server_name {ВАШ_ДОМЕН};

    ssl_certificate /etc/letsencrypt/live/{ВАШ_ДОМЕН}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/{ВАШ_ДОМЕН}/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    location /webhook {
        proxy_pass http://app:8080/webhook;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Telegram-Bot-Api-Secret-Token $http_x_telegram_bot_api_secret_token;

        proxy_connect_timeout 5s;
        proxy_send_timeout 5s;
        proxy_read_timeout 5s;

        limit_except POST {
            deny all;
        }
    }

    location / {
        proxy_pass http://app:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 5s;
        proxy_send_timeout 5s;
        proxy_read_timeout 5s;
    }
}
```
1. **Соберите Docker-контейнеры: В корне проекта выполните**
```bash
docker-compose up -d
```
2. **Настройте Webhook в Telegram: Выполните запрос**
```bash
curl -X POST "https://api.telegram.org/bot{ВАШ_ТОКЕН_БОТА}/setWebhook?url={ВАШ_ДОМЕН}&secret_token={ВАШ_СЕКРЕТНЫЙ_КЛЮЧ}"
```

## Примечания
- **Для ветки review_dev_to_webhook убедитесь, что домен настроен и доступен по HTTPS.**
- **Используйте letsencrypt для генерации SSL-сертификатов.**
