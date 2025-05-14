![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

<!-- этот файл можно и нужно менять -->

Проект сделан в рамках курса Академия Бэкенда.

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:

* Bot
* Scrapper

Для работы требуется БД `PostgreSQL`. Присутствует опциональная зависимость на `Kafka`.

Для дополнительной справки: [HELP.md](./HELP.md)

Приложение поддерживает только трекинг вопросов англоязычного StackOverflow (stackoverflow.com) и репозиториев GitHub (
github.com)

## Инструкция по запуску

0) Установите в своем окружении переменные с именем

``GITHUB_TOKEN`` - токен авторизации GitHub

``SO_TOKEN_KEY`` - ключ от StackOverflow

``SO_ACCESS_TOKEN`` - токен авторизации StackOverflow

``TELEGRAM_TOKEN`` - токен телеграмм бота

Для запуска PostgreSql используйте docker-compose в корне проекта

``
docker-compose up -d
``

Перед запуском установите в своем окружении переменные с именем

``PGADMIN_EMAIL`` - логин для авторизации в pgAdmin

``PGADMIN_PASSWORD`` - пароль для авторизации в pgAdmin

``DB_USER`` - пользователь для подключения к бд

``DB_PASSWORD`` - пароль для подключения к бд

Также может потребоваться в docker-compose.yaml вручную заменить имя пользователя и пароль в строчках

      - --username=${DB_USER}
      - --password=${DB_PASSWORD}

Также при желании можно заменить url подключения к бд

1) Откройте два терминала
2) В первом терминале из корня репозитория перейдите в ./bot и введите команду

``
mvn spring-boot:run
``

3) Во втором терминале из корня репозитория перейдите в ./scrapper и введите команду

``
mvn spring-boot:run
``

Проект запущен!
