# Car Sharing System

[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue.svg)](https://postgresql.org)
[![Code Style](https://img.shields.io/badge/code%20style-Google%20Java%20Format-000000.svg)](https://google.github.io/styleguide/javaguide.html)

## 📝 Опис проєкту

Система для оренди автомобілів з унікальною функцією **Split Access** – спільною орендою одним автомобілем кількома водіями.

### Ключові можливості

- 🔐 Реєстрація та автентифікація (JWT)
- 🔍 Пошук та фільтрація автомобілів
- 📅 Бронювання авто на вибрані дати
- 💳 Оплата через Stripe
- 👥 **Split Access** – додавання спів-водіїв до бронювання
- ⭐ Відгуки та рейтинги

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

# 2. Запуск PostgreSQL (Docker)
docker run -d \
  --name car-sharing-db \
  -e POSTGRES_DB=car_sharing_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:14

# 3. Налаштування змінних оточення
cp .env.example .env
# Відредагуйте .env з вашими значеннями

# 4. Збірка проєкту
mvn clean compile

# 5. Запуск тестів
mvn test

# 6. Запуск додатку
mvn spring-boot:run
