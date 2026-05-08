// src/services/auth.service.js
import axios from 'axios';

// Точна адреса з curl-запитів твого бекендера (порт 8081)
const USER_API_URL = 'http://localhost:8081/user/v1';

export const authService = {

    // Авторизація (Логін)
    login: async (credentials) => {
        const response = await axios.post(`${USER_API_URL}/login`, credentials);

        // Бекенд зараз повертає { message: "...", user: { ... } }
        // Тому ми зберігаємо саме об'єкт user, щоб знати, хто залогінений
        if (response.data && response.data.user) {
            localStorage.setItem('user', JSON.stringify(response.data.user));
        }

        return response.data;
    },

    // Реєстрація
    register: async (userData) => {
        const response = await axios.post(`${USER_API_URL}/register`, userData);
        return response.data;
    },

    // Вихід
    logout: () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token'); // Залишаємо на майбутнє, коли додасте JWT
    }
};
