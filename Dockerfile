FROM openjdk:17-alpine3.14

# Создаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем jar-файл в контейнер
COPY target/social-network-authorization-0.0.1-SNAPSHOT.jar /app/social-network-authorization-0.0.1-SNAPSHOT.jar

# Определяем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "social-network-authorization-0.0.1-SNAPSHOT.jar"]
