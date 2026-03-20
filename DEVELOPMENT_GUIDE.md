# Development Guide - Manual Policies

## 🌿 Branching Policy (GitFlow)

**Заборонено прямі коміти в `main`!**

Вся розробка ведеться через окремі гілки:
- `main` – стабільна версія (тільки через Pull Requests)
- `feature/XXX-task-description` – для нових функцій
- `fix/XXX-bug-description` – для виправлення помилок
- `docs/XXX-description` – для документації
- `refactor/XXX-description` – для рефакторингу

## 📝 Naming Conventions

### Commit Messages
Кожен коміт має починатися з одного з префіксів:

| Prefix | Використання | Приклад |
|--------|--------------|---------|
| `feat:` | Нова функціональність | `feat: add car booking endpoint` |
| `fix:` | Виправлення помилки | `fix: resolve authentication bug` |
| `docs:` | Зміни в документації | `docs: update README with setup instructions` |
| `refactor:` | Рефакторинг коду | `refactor: simplify booking service logic` |

### Branch Names
Гілки мають містити ID завдання (з Jira/Trello) та короткий опис:
