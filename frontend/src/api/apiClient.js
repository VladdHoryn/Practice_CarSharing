import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost:8085', // Повертаємо 8085, бо тепер бекенд у Докері!
    headers: {
        'Content-Type': 'application/json',
    },
});

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
