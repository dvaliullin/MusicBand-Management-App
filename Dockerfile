# Этап 1: Сборка jar-файлов с помощью Maven и JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Копируем конфигурационные файлы сборки
COPY pom.xml .
COPY common/pom.xml ./common/
COPY client/pom.xml ./client/
COPY server/pom.xml ./server/

# Скачиваем зависимости (кэширование слоев)
RUN mvn dependency:go-offline -B

# Копируем исходный код модулей
COPY common ./common
COPY server ./server

# Собираем только модуль сервера и его зависимости (common)
RUN mvn clean package -pl server -am -DskipTests

# Этап 2: Создание легковесного образа среды выполнения (JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный исполняемый jar-файл из этапа сборки
COPY --from=build /app/server/target/server.jar /app/server.jar

# Открываем порт сервера для внешних подключений
EXPOSE 8888

# Запуск сервера
ENTRYPOINT ["java", "-jar", "/app/server.jar"]