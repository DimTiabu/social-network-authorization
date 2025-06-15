FROM openjdk:17-alpine3.14

# Создаем рабочую директорию внутри контейнера
WORKDIR /app

# Устанавливаем freetype и шрифты, только если они еще не установлены
# apk info выводит список установленных пакетов, поэтому если нужного пакета нет – выполняем установку.
RUN if ! apk info | grep -q 'freetype'; then apk add --no-cache freetype; fi && \
    if ! apk info | grep -q 'ttf-freefont'; then apk add --no-cache ttf-freefont; fi

# Копируем jar-файл в контейнер
COPY target/social-network-authorization-0.0.1-SNAPSHOT.jar /app/social-network-authorization-0.0.1-SNAPSHOT.jar

# Определяем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "social-network-authorization-0.0.1-SNAPSHOT.jar"]