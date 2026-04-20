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



**Приклад:** `feature/CS-15-booking-system`

## ✅ Code Review Policy

**Критичне правило:** Жоден Pull Request (PR) не може бути прийнятий без **текстового коментаря "Approve"** від іншого учасника команди.

### Процес Code Review:
1. Автор створює PR і призначає **Reviewer**
2. Reviewer перевіряє код і залишає коментар:
   - **"Approve"** – якщо все добре, можна мержити
   - Коментарі з правками – якщо потрібні зміни
3. Після отримання **"Approve"** автор або reviewer може виконати merge
4. **Не можна** мержити власний PR без чужого схвалення

### Шаблон для reviewer:


## 🔒 Branch Protection Rules (GitHub Settings)

Для захисту гілки `main` у налаштуваннях GitHub увімкнено:
- ✅ Require a pull request before merging
- ✅ Require approvals (1 reviewer)
- ✅ Dismiss stale reviews when new commits are pushed
- ✅ Require conversation resolution before merging

---

*Ці правила є обов'язковими для всіх учасників команди.*


## 📋 Правила розробки

Усі учасники команди зобов'язані дотримуватись [Development Guide](DEVELOPMENT_GUIDE.md) – регламенту роботи з Git, іменування комітів та Code Review.
