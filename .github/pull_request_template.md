```markdown
# Pull Request

## 📝 Опис змін

<!-- Стисло опишіть, що саме було зроблено -->

## 🏷 Тип зміни

- [ ] Bug fix (виправлення помилки)
- [ ] Feature (нова функціональність)
- [ ] Refactoring (рефакторинг без зміни логіки)
- [ ] Documentation (зміни в документації)
- [ ] Database migration (зміни в схемі БД)

## 🔗 Пов'язане завдання

<!-- Jira ID або GitHub Issue -->
Closes #(номер_іш'ю)

## ✅ Self-Review Checklist

### Код
- [ ] Код відповідає [Google Java Style Guide](CODE_STYLE.md)
- [ ] Checkstyle не видає помилок (`mvn checkstyle:check`)
- [ ] SpotBugs не знаходить проблем (`mvn spotbugs:check`)
- [ ] Форматування виконано (`mvn spotless:apply`)
- [ ] Відсутні закоментовані фрагменти коду
- [ ] Відсутні `System.out.println()` (використовуємо `log.debug()`)
- [ ] Використано Lombok (@Slf4j, @RequiredArgsConstructor)
- [ ] Javadoc додано для всіх публічних методів

### Тестування
- [ ] Юніт-тести написані для нової логіки
- [ ] Інтеграційні тести для API
- [ ] Тести проходять успішно (`mvn test`)
- [ ] Покриття коду не нижче 80% (`mvn jacoco:report`)

### База даних
- [ ] Flyway міграції створені (якщо змінювалась схема)
- [ ] Зміни в БД зворотньо сумісні
- [ ] Індекси додано для нових зовнішніх ключів

### Конфігурація
- [ ] Змінні оточення додані в `.env.example`
- [ ] Оновлена документація (Swagger/OpenAPI)
- [ ] Профілі Spring (`dev`, `prod`) враховані

## 🧪 Як тестувати

<!-- Опишіть кроки для ручного тестування -->
1. Запустіть `mvn clean test`
2. Надшліть запит на `POST /api/v1/bookings`
3. Перевірте відповідь

```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"carId": "123", "startDate": "2024-04-01T10:00:00"}'
