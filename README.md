# 🚗 CarLink Booking (Microservices Car Sharing System)

[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg)](https://reactjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue.svg)](https://postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

## 📝 Опис проєкту

**CarLink Booking** — це комплексна веб-платформа для швидкого та прозорого бронювання автомобілів, побудована на мікросервісній архітектурі. 

🔥 **Killer Feature проєкту: Split Access**. Унікальна система спільної оренди одним автомобілем кількома водіями. Вона дозволяє генерувати `Driver Code`, додавати співводіїв до одного бронювання та прозоро розподіляти фінансову відповідальність між ними.

### Ключові можливості (MVP)
- 👥 **Split Access** – додавання співводіїв до бронювання через унікальні токени.
- 🔐 Рольова автентифікація через Keycloak (Admin, Owner, Customer).
- 🔍 Smart-пошук та фільтрація автомобілів.
- 📊 **Dual Dashboard** – окремі кабінети для користувачів та прокатних компаній (з аналітикою доходів).
- 💳 Імітація онлайн-оплати (Stripe-ready архітектура).

---

## 📂 Структура репозиторію

Проєкт складається з незалежних модулів (мікросервісів) та фронтенду:

```text
Practice_CarSharing/
├── backend/                  # Бекенд (Spring Boot мікросервіси)
│   ├── api-gateway/          # Точка входу, маршрутизація запитів (Port: 8080)
│   ├── user-service/         # Управління користувачами, генерація Driver Code
│   ├── car-service/          # Каталог авто та Business логіка автопарку
│   ├── booking-service/      # Ядро системи: бронювання та Split Access
│   ├── payment-service/      # Обробка транзакцій
│   └── analytics-service/    # Агрегація даних для B2B дашбордів
├── frontend/                 # Клієнтська частина (React SPA)
├── docker/                   # Конфігурації для інфраструктури
│   └── keycloak/             # Файли налаштувань (Realm) для Keycloak
├── docker-compose.yml        # Головний файл розгортання системи
└── .env.example              # Шаблон змінних середовища

---

## 🛠 Технологічний стек

| Компонент | Технологія |
|-----------|-----------|
| **Мова** | Java 21 |
| **Framework** | Spring Boot 3.2.4 |
| **Database** | PostgreSQL 14+ |
| **Migration** | Flyway |
| **ORM** | Hibernate / JPA |
| **Security** | Spring Security + JWT |
| **Build Tool** | Maven |
| **Code Style** | Google Java Format |
| **Linter** | Checkstyle + SpotBugs |
| **Testing** | JUnit 5 + Testcontainers |
| **Code Coverage** | JaCoCo |

## 🚀 Швидкий старт

### Передумови

- Java 21 (JDK)
- Maven 3.9+
- Docker (для PostgreSQL)
- PostgreSQL 14+ (або Docker)

### Встановлення

```bash
# 1. Клонування репозиторію
git clone https://github.com/vladdhoryn/Practice_CarSharing.git
cd Practice_CarSharing

# 2. Збірка проєкту
mvn clean package

# 3. Запуск Docker
docker-compose up --build

# 6. Запуск додатку
mvn spring-boot:run
