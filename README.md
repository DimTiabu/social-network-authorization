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
![Static Badge](https://img.shields.io/badge/PostgreSQL-grey)
![Static Badge](https://img.shields.io/badge/Eureka_Client-grey)
![Static Badge](https://img.shields.io/badge/Liquibase-grey)
![Static Badge](https://img.shields.io/badge/Captcha_(com.github.cage)-grey)

## Функциональность
### Аутентификация

- Стандартная аутентификация по email и паролю.
- Поддержка входа по Telegram Chat ID.
- Генерация JWT и refresh-токенов.
- Аутентификация через AuthenticationManager.

### Обновление токенов

- Автоматическая генерация пары access/refresh токенов.
- Проверка валидности и срока действия refresh-токенов.

### Восстановление пароля
- Генерация временного пароля и отправка HTML-письма через SMTP (Mail.ru).
- HTML-шаблон: resources/templates/recovery_email.html.

### Смена пароля и email
- Подтверждение старого пароля перед сменой.
- Валидация совпадения новых паролей.
- Отправка Kafka-сообщения при изменении email.

### Captcha
- Генерация captcha-картинки и соответствующего кода с использованием библиотеки Cage.
- Формат изображения: data:image/jpeg;base64,....

## Инструкция по запуску проекта на сервере

### Предварительные требования:

- Установленный JDK (рекомендуется JDK 17)
- Установленный Maven
- Docker
- Git
- Удаленный сервер

### Шаги для запуска:

1. *Клонирование репозитория:*

```sh
   git clone https://github.com/DimTiabu/social-network-authorization.git
```

2. *Выбор активного профиля dev в файле [application.yaml](application.yaml).*

```
  profiles:
    active: dev
#    active: test
#    active: testEureka
```

3. *Установка номера хоста вместо 'localhost' в файле [application-dev.yaml](application-dev.yaml).* 
```
host: localhost
```
4. *Установка логина и пароля для доступа к БД в файле [application-dev.yaml](application-dev.yaml).* Например:
```
  datasource:
    url: jdbc:postgresql://${host}:5432/authorization_db?currentSchema=schema_authorization
    username: postgre_user
    password: postgre_secret_password
```
5. *Добавление переменных окружения в файле [Dockerfile](Dockerfile).* Например:
```
ENV MAIL_USER=test@mail.ru
ENV MAIL_PASSWORD=testPassword
```
Здесь нужно указать электронную почту и пароль для SMTP-рассылки.

[Подробнее о SMTP на примере сервиса mail.ru](https://help.mail.ru/mail/mailer/password/#create:~:text=%D0%B8%D0%BB%D0%B8%20%D0%B4%D1%80%D1%83%D0%B3%D0%BE%D0%B9%20%D0%BA%D0%BB%D0%B8%D0%B5%D0%BD%D1%82.-,%D0%9A%D0%B0%D0%BA%20%D1%81%D0%BE%D0%B7%D0%B4%D0%B0%D1%82%D1%8C%20%D0%BF%D0%B0%D1%80%D0%BE%D0%BB%D1%8C%20%D0%B4%D0%BB%D1%8F%20%D0%B2%D0%BD%D0%B5%D1%88%D0%BD%D0%B5%D0%B3%D0%BE%20%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F,-%D0%9F%D0%B0%D1%80%D0%BE%D0%BB%D1%8C%20%D0%B4%D0%BB%D1%8F%20%D0%B2%D0%BD%D0%B5%D1%88%D0%BD%D0%B5%D0%B3%D0%BE).
6. *Сборка проекта*

```sh
    mvn clean package
```

7. *Сборка docker-образа*

```sh
    docker build -t your_username/myapp:latest .
```
8. *Авторизация в Docker Hub:*

```sh
    docker login
```

9. *Отправка образа на Docker Hub:*

```sh
     docker push your_username/myapp:latest
```
После этого образ будет доступен в вашем аккаунте на Docker Hub.

*Вы можете автоматизировать процесс с помощью CI/CD-систем 
(например, TeamCity, GitHub Actions, GitLab CI или Jenkins), 
чтобы сборка и отправка образа происходили автоматически 
при изменениях в кодовой базе.*

10. *Запуск контейнера с Docker Hub на сервере:*
```sh
    docker pull dmitrtiab/auth-service:latest
```
---

Спасибо за использование приложения "Социальная сеть"!
