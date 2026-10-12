# MusicBand-Management-App

Клиент-серверное приложение для управления коллекцией музыкальных групп (`MusicBand`).

## 📌 Основные возможности

### Серверная часть
* **Неблокирующий сетевой ввод-вывод (Java NIO)**
* **Многопоточная архитектура**
* **База данных, СУБД PostgreSQL**

### Клиентская часть (Swing GUI)
* **Локализация**:
  * Поддержка 4 языков: **Русский**, **Английский (Новая Зеландия)**, **Чешский**, **Датский**.
* **Табличное представление объектов**
* **Интерактивная карта (OpenStreetMap)**

---

## 🛠 Стек технологий

* **Язык**: Java 17
* **Сборщик проекта**: Apache Maven
* **GUI**: Swing, AWT, JXMapViewer2
* **Сетевой стек**: Java NIO
* **База данных**: PostgreSQL, JDBC Driver
* **Логирование**: SLF4J, Logback
* **Контейнеризация**: Docker

---

## 📂 Структура проекта

```text
├── common/               # Общие классы (модели данных, шаблоны запросов к серверу и ответов, исключения)
│   └── src/main/java/ru/itmo/lab8/common/
│       ├── data/         # MusicBand, Coordinates, Label, MusicGenre
│       ├── exceptions/   # UserAlreadyExistsException, IllegalValueException и др.
│       └── util/         # Request, Response, AuthRequest, AuthResponse, Checker
├── server/               # Серверный модуль (обработка запросов, СУБД, команды)
│   └── src/main/java/ru/itmo/lab8/server/
│       ├── commands/     # Реализация команд (Insert, Update, RemoveKey, Clear и др.)
│       ├── database/     # DatabaseManager, UsersManager, SQLQueryManager, PasswordEncoder
│       ├── network/      # ServerManager, Handler, Receiver
│       └── util/         # CollectionManager, CommandManager
├── client/               # Клиентский модуль (GUI Swing, карта, локализация)
│   └── src/main/java/ru/itmo/lab8/client/
│       ├── gui/          # MainWindow, AuthDialog, BandTable, VisualizationPanel и др.
│       ├── localization/ # ResourceBundle_ru, ResourceBundle_cs, ResourceBundle_da, ResourceBundle_en_NZ
│       └── network/      # ConnectionManager, Requester
├── Dockerfile            # Сборка образа сервера
├── docker-compose.yml    # Оркестрация контейнеров PostgreSQL и Сервера
└── pom.xml               # Родительский POM-файл Maven
```

---

## 🚀 Инструкция по сборке и запуску

### Вариант 1. Запуск через Docker Compose

1. **Клонируйте репозиторий**:
   ```bash
   git clone https://github.com/dvaliullin/MusicBand-Management-App.git
   cd MusicBand-Managment-App
   ```

2. **Запустите контейнеры**:
   ```bash
   docker-compose up --build
   ```
   *База данных будет доступна по порту `5432`, сервер — по порту `8888` на хосте `0.0.0.0`.*

3. **Запустите клиент**:
     ```bash
     mvn clean package -pl client -am
     java -jar client/target/client.jar
     ```

---

### Вариант 2. Локальный запуск

1. **Убедитесь, что установлена PostgreSQL**:
   Создайте базу данных `musicband_db` ([скрипт](musicband.sql)) или настройте параметры под существующую базу.

2. **Соберите проект через Maven**:
   ```bash
   mvn clean package -pl common,server,client
   ```

3. **Запустите сервер**:
   ```bash
   # Для Linux / macOS:
   export SERVER_HOST=localhost
   export SERVER_PORT=8888
   export DB_URL=jdbc:postgresql://localhost:5432/musicband_db
   export DB_USER=postgres
   export DB_PASSWORD=your_password
   java -jar server/target/server.jar

   # Для Windows (PowerShell):
   $env:SERVER_HOST="localhost"
   $env:SERVER_PORT="8888"
   $env:DB_URL="jdbc:postgresql://localhost:5432/musicband_db"
   $env:DB_USER="postgres"
   $env:DB_PASSWORD="your_password"
   java -jar server/target/server.jar
   ```

4. **Запустите клиентское приложение**:
   ```bash
   java -jar client/target/client.jar
   ```

---

## 📋 Перечень команд

| Команда | Описание |
| :--- | :--- |
| `help` | Вывести справку по доступным командам|
| `info` | Показать информацию о коллекции (тип, дата инициализации, количество элементов и т.д.) |
| `show` | Показать все элементы коллекции |
| `insert` | Добавить новый элемент с заданным уникальным ключом |
| `update` | Обновить значение элемента коллекции, `id` которого равен заданному |
| `remove_key` | Удалить элемент из коллекции по его ключу |
| `clear` | Удалить все элементы, принадлежащие текущему пользователю |
| `execute_script` | Считать и исполнить скрипт из указанного файла |
| `remove_greater` | Удалить из коллекции все элементы, превышающие заданный |
| `replace_if_greater` | Заменить значение по ключу, если новое значение больше старого |
| `remove_greater_key` | Удалить из коллекции все элементы, ключ которых превышает заданный |
| `count_greater_than_number_of_participants` | Вывести количество элементов с числом участников больше заданного |
| `print_ascending` | Вывести элементы коллекции в порядке возрастания |
| `print_field_descending_number_of_participants` | Вывести значения поля участников всех элементов в порядке убывания |
