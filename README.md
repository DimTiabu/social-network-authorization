# Social Network Authorization

## Описание проекта
Сервис аутентификации и авторизации для социальной сети, 
реализованный с использованием Spring Boot. 

Поддерживает JWT, refresh-токены, интеграцию с Telegram 
и функциональность восстановления пароля через email.
Сервис является частью микросервисного приложения. 
Для взаимодействия с другими сервисами реализована интеграция с Kafka. 

## Стэк используемых технологий
![Static Badge](https://img.shields.io/badge/Java-17-blue)
![Static Badge](https://img.shields.io/badge/Spring_Boot-3-green)
![Static Badge](https://img.shields.io/badge/Spring_Security-grey)
![Static Badge](https://img.shields.io/badge/JWT_(JJWT)-grey)
![Static Badge](https://img.shields.io/badge/Kafka-grey)
![Static Badge](https://img.shields.io/badge/Redis-grey)
![Static Badge](https://img.shields.io/badge/PostgreSQL-grey)

![Static Badge](https://img.shields.io/badge/SMTP-grey)
![Static Badge](https://img.shields.io/badge/Eureka_Client-grey)
![Static Badge](https://img.shields.io/badge/Liquibase-grey)
![Static Badge](https://img.shields.io/badge/Captcha-Cage_library-grey)

## Функциональность
### Аутентификация

- Стандартная аутентификация по email и паролю.
- Поддержка входа по Telegram Chat ID.
- Генерация JWT и refresh-токенов.
- Аутентификация через AuthenticationManager.

### Обновление токенов

- Автоматическая генерация пары access- и refresh-токенов.
- Проверка валидности и срока действия refresh-токенов.

### Восстановление пароля
- Генерация временного пароля и отправка HTML-письма через SMTP (Mail.ru).
- [Шаблон письма](src/main/resources/templates/recovery_email.html).

### Смена пароля и email
- Подтверждение старого пароля перед сменой.
- Валидация совпадения новых паролей.
- Отправка Kafka-сообщения о смене email в другие микросервисы.

### Captcha
- Генерация изображения CAPTCHA и соответствующего кода с использованием библиотеки Cage.

## Инструкция по запуску проекта на сервере

### Предварительные требования:

- Установленный JDK (рекомендуется JDK 17)
- Установленный Maven
- Docker
- Git
- Удаленный сервер c Docker

### Шаги для запуска:

1. *Клонирование репозитория:*


```sh
   git clone https://github.com/DimTiabu/social-network-authorization.git
```

2. *Выбор активного профиля dev в файле [application.yml](src/main/resources/application.yml).*

```
  profiles:
    active: dev
```

3. *Указание имени хоста вместо 'localhost' в файле [application-dev.yml](src/main/resources/application-dev.yml).* 
```
host: localhost
```
4. *Установка логина и пароля для доступа к БД в файле [application-dev.yml](src/main/resources/application-dev.yml).* Например:
```
  datasource:
    url: jdbc:postgresql://${host}:5432/authorization_db?currentSchema=schema_authorization
    username: postgres
    password: postgre_secret_password
```

5. *Сборка проекта*

```sh
    mvn clean package
```

6. *Сборка docker-образа*

```sh
    docker build -t your_username/auth-service:latest .
```
7. *Авторизация в Docker Hub:*

```sh
    docker login
```

8. *Отправка образа на Docker Hub:*

```sh
     docker push your_username/auth-service:latest
```
После этого образ будет доступен в вашем аккаунте на Docker Hub.

*Можно автоматизировать процесс с помощью CI/CD-систем 
(например, TeamCity, GitHub Actions, GitLab CI или Jenkins), 
чтобы сборка и отправка образа происходили автоматически 
при изменениях в кодовой базе.*

9. Размещение файла `.env` на сервере*

Создайте `.env` рядом с местом запуска контейнера, например:

```
# .env
MAIL_USER=test@mail.ru
MAIL_PASSWORD=testPassword
```
Здесь нужно указать электронную почту и пароль для SMTP-рассылки.

Подробнее о SMTP на примере сервиса mail.ru можно прочитать [здесь](https://help.mail.ru/mail/mailer/password/#create:~:text=%D0%B8%D0%BB%D0%B8%20%D0%B4%D1%80%D1%83%D0%B3%D0%BE%D0%B9%20%D0%BA%D0%BB%D0%B8%D0%B5%D0%BD%D1%82.-,%D0%9A%D0%B0%D0%BA%20%D1%81%D0%BE%D0%B7%D0%B4%D0%B0%D1%82%D1%8C%20%D0%BF%D0%B0%D1%80%D0%BE%D0%BB%D1%8C%20%D0%B4%D0%BB%D1%8F%20%D0%B2%D0%BD%D0%B5%D1%88%D0%BD%D0%B5%D0%B3%D0%BE%20%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F,-%D0%9F%D0%B0%D1%80%D0%BE%D0%BB%D1%8C%20%D0%B4%D0%BB%D1%8F%20%D0%B2%D0%BD%D0%B5%D1%88%D0%BD%D0%B5%D0%B3%D0%BE).


10. *Запуск контейнера с Docker Hub на сервере:*
```sh
    docker pull dmitrtiab/auth-service:latest
    docker run -d \
  --name auth-service \
  --env-file /home/user/app/.env \
  -p 8081:8081 \
  your_username/auth-service:latest
```
📌 Важно: файл `.env` не должен попадать в Docker-образ 
и не используется при `docker build`.  
Убедитесь, что `.env` находится на сервере рядом с местом запуска, 
и передавайте его через флаг `--env-file`

---
### Внимание!

Для правильной работы сервиса авторизации необходимо запустить на сервере
сервисы gateway, discovery, фронтенд-сервис, а также Redis, PostgreSQL,
Apache Kafka и Apache ZooKeeper при помощи следующих команд:
```sh
    docker pull dmitrtiab/social-network-gateway:latest
    docker pull dmitrtiab/social-network-discovery:latest
    docker pull dmitrtiab/social-network-frontend:latest
    docker pull redis:7.0.12
    docker pull postgres:16.2-alpine
    docker pull wurstmeister/kafka:2.13-2.6.3
    docker pull confluentinc/cp-zookeeper:5.5.0
    
    docker run -d \
  --name gateway-service \
  -p 8080:8080 \
  --network app-network \
  dmitrtiab/social-network-gateway:latest
  
    docker run -d \
  --name discovery-service \
  --restart always \
  -p 8761:8761 \
  --network app-network \
  dmitrtiab/social-network-discovery:latest

    docker run -d \
  --name frontend \
  --restart always \
  -p 80:80 \
  --network app-network \
  dmitrtiab/social-network-frontend:latest
   
    docker run -d \
  --name redis \
  --restart always \
  -p 6379:6379 \
  --network app-network \
  redis:7.0.12

    docker run -d \
  --name postgres \
  --restart always \
  -e POSTGRES_USER=postgres \
  -p 5432:5432 \
  --network app-network \
  postgres:16.2-alpine
  
    docker run -d \
  --name kafka \
  --restart always \
  -p 9092:9092 -p 9094:9094 \
  -e KAFKA_ADVERTISED_HOST_NAME=kafka \
  -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=INSIDE://:9092,OUTSIDE://SERVER_IP:9094 \
  -e KAFKA_LISTENERS=INSIDE://:9092,OUTSIDE://:9094 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=INSIDE \
  --network app-network \
  wurstmeister/kafka:2.13-2.6.3
# Вместо SERVER_IP укажите внешний IP-адрес вашего сервера.

    docker run -d \
  --name zookeeper \
  --restart always \
  -p 2181:2181 \
  -e ZOOKEEPER_CLIENT_PORT=2181 \
  -e ZOOKEEPER_TICK_TIME=2000 \
  --network app-network \
  confluentinc/cp-zookeeper:5.5.0

```
---
## Тестирование

Проект содержит модульные и интеграционные тесты, покрывающие:

- Аутентификацию и генерацию токенов
- Восстановление пароля и отправку писем через SMTP
- Kafka-события (например, смена email)
- REST API эндпоинты (`/auth/login`, `/auth/register`, `/auth/recovery` и др.)

Для запуска всех тестов:
```sh
    mvn clean test
```