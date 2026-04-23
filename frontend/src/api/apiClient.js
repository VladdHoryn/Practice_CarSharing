// Шлях: frontend/src/api/apiClient.js
import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8085', // Порт твого car-service
  headers: {
    'Content-Type': 'application/json',
  },
});

// Додаємо токен (знадобиться пізніше для авторизації)
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default apiClient;
