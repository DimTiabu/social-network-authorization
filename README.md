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
- Поддержка входа по Telegram Chat ID (при наличии).
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

## Инструкция по локальному запуску проекта

### Предварительные требования:

- Установленный JDK (рекомендуется JDK 17)
- Установленный Maven
- PostgreSQL
- Git

### Шаги для запуска:

1. *Клонирование репозитория:*

```sh
   git clone https://github.com/DimTiabu/social-network-authorization.git
```

2. *Выбор активного профиля test в файле [application.yaml](application.yaml).* Например:

```
  profiles:
#    active: dev
    active: test
#    active: testEureka
```

3. *Установка логина и пароля для доступа к БД в файле [application-test.yaml](application-test.yaml).* Например:
```
  datasource:
    url: jdbc:postgresql://localhost:5432/authorization_db
    username: postgres
    password: postgres
```
4. *Переход в директорию расположения файла docker-compose:*

```sh
    cd social-network-authorization/docker
```

5. *Запуск docker-compose:*

```sh
    docker-compose up -d
```
⏳ Проверка окончания запуска всех контейнеров Kafka, Redis, PostgreSQL 
и Zookeeper:

```sh
    docker ps
```
**Переходить к следующему пункту можно, 
когда все контейнеры будут в статусе Up.**

6. *Переход в директорию репозитория:*

```sh
   cd ..
```

7. *Добавление переменных окружения:*

```sh
     $env:MAIL_USER="mail@mail.ru"
     $env:MAIL_PASSWORD="passwordForSmtp"
```
Здесь нужно указать электронную почту и пароль для SMTP-рассылки. 

[Подробнее о SMTP на примере сервиса mail.ru](https://help.mail.ru/mail/mailer/password/#create:~:text=%D0%B8%D0%BB%D0%B8%20%D0%B4%D1%80%D1%83%D0%B3%D0%BE%D0%B9%20%D0%BA%D0%BB%D0%B8%D0%B5%D0%BD%D1%82.-,%D0%9A%D0%B0%D0%BA%20%D1%81%D0%BE%D0%B7%D0%B4%D0%B0%D1%82%D1%8C%20%D0%BF%D0%B0%D1%80%D0%BE%D0%BB%D1%8C%20%D0%B4%D0%BB%D1%8F%20%D0%B2%D0%BD%D0%B5%D1%88%D0%BD%D0%B5%D0%B3%D0%BE%20%D0%BF%D1%80%D0%B8%D0%BB%D0%BE%D0%B6%D0%B5%D0%BD%D0%B8%D1%8F,-%D0%9F%D0%B0%D1%80%D0%BE%D0%BB%D1%8C%20%D0%B4%D0%BB%D1%8F%20%D0%B2%D0%BD%D0%B5%D1%88%D0%BD%D0%B5%D0%B3%D0%BE).
8. *Сборка проекта:*


```sh
    mvn clean install
```

7. *Запуск приложения:*

sh
```
mvn spring-boot:run
```

### Использование API

Приложение предоставляет REST API для взаимодействия
с функционалом социальной сети.

Для начала работы с приложением откройте браузер
и перейдите по адресу: http://localhost:8080/.

     

---

Спасибо за использование приложения "Поисковая система"! Удачного поиска!
