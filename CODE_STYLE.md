# Code Style Guide - Car Sharing Project (Java + PostgreSQL)

## Загальні правила

### Boolean змінні
Починаються з префіксів:
- `is` / `has` / `can`
- Приклад: `isActive`, `hasPermission`, `canEdit`

### Абревіатури
- В Java: `getXmlParser()` → `getXmlParser()` (тільки перша літера велика)
- Для стандартних абревіатур (XML, HTML, URL): пишіть `XmlParser`, `HtmlGenerator`, `UrlBuilder`
- Виняток: `getId()` (не `getID`), `setUrl()` (не `setURL`)

### Заборонені назви (Сміттєві)
❌ `data`, `info`, `temp`, `tmp`, `stuff`, `misc`, `holder`, `wrapper`
✅ `userData`, `bookingInfo`, `temporaryFile`, `paymentDetails`

---

## Backend (Java)

### Папки (Packages)
- `lowercase.snake_case` (краще `lowercase.without.underscores`)
- Приклад: `com.carsharing.users`, `com.carsharing.bookings`, `com.carsharing.payments`
- Структура: `com.carsharing.{module}.{layer}`

### Файли класів
- `PascalCase.java`
- Приклад: `UserService.java`, `CarRepository.java`, `BookingController.java`

### Класи
- `PascalCase`
- Іменники або іменникові фрази
- Приклад: `UserService`, `CarRepository`, `BookingEntity`, `PaymentController`

### Інтерфейси
- `PascalCase`
- Без префікса `I` (краще: `UserRepository`, а не `IUserRepository`)
- Для функціональних інтерфейсів можна використовувати суфікс `-able`: `Rentable`, `Cancellable`

### Методи
- `camelCase`
- Дієслова або дієслівні фрази
- Приклад: `getUserById()`, `createBooking()`, `calculateTotalPrice()`, `isValid()`

### Змінні та параметри
- `camelCase`
- Приклад: `userId`, `totalPrice`, `isActive`, `bookingStatus`

### Константи
- `UPPER_SNAKE_CASE`
- `static final` або `enum`
- Приклад: `MAX_LOGIN_ATTEMPTS`, `DEFAULT_PAGE_SIZE`, `STATUS_ACTIVE`

### Приватні поля класу
- `camelCase`
- Без префікса `_` або `m_`
- Приклад: `private String email;`, `private boolean isActive;`

### Лямбда-вирази
- Використовувати повні імена параметрів
- ❌ `(a, b) -> a + b`
- ✅ `(first, second) -> first + second`

### Відступи
- **2 пробіли** (Google Java Style Guide)
- Не використовувати табуляцію

### Максимальна довжина рядка
- **100 символів** (Google Style)
- Переносити рядки з вирівнюванням

### Документація (Javadoc)
- Обов'язково для всіх публічних класів, методів (крім простих getter/setter)
- Використовувати стандартні теги: `@param`, `@return`, `@throws`, `@since`

```java
/**
 * Calculates the total rental price with optional discount.
 *
 * @param pricePerDay daily rental price, must be positive
 * @param days number of rental days, must be at least 1
 * @param discount discount percentage (0-100), default 0
 * @return total price after discount
 * @throws IllegalArgumentException if discount is out of range
 * @since 1.0.0
 */
public BigDecimal calculateTotalPrice(BigDecimal pricePerDay, int days, int discount) {
    // implementation
}
