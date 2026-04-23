import axios from 'axios';

// Заміни на URL твого Spring Boot бекенду (зазвичай це порт 8080)
const API_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Інтерцептор ЗАПИТУ: автоматично додає JWT токен з localStorage
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Якщо бекенд каже 401 Unauthorized - видаляємо токен і кидаємо на логін
            localStorage.removeItem('token');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default api;
