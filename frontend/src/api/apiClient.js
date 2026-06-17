import axios from 'axios';

const KEYCLOAK_TOKEN_URL = 'http://localhost:8100/realms/carsharing-realm/protocol/openid-connect/token';

const apiClient = axios.create({
    baseURL: 'http://localhost:8100', // Наш API Gateway
});

// Допоміжний прапорець, щоб уникнути нескінченного циклу оновлень токена
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

apiClient.interceptors.request.use(
    (config) => {
        const authTokensStr = localStorage.getItem('auth_tokens');
        if (authTokensStr) {
            try {
                const { access_token } = JSON.parse(authTokensStr);
                if (access_token) {
                    // 🚨 КРИТИЧНО: Слово 'Bearer' має бути з великої літери, а після нього – ОДИН ПРОБІЛ
                    config.headers['Authorization'] = `Bearer ${access_token}`;
                }
            } catch (e) {
                console.error("Помилка парсингу токена в перехоплювачі:", e);
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (originalRequest.url.includes('/openid-connect/token')) {
            return Promise.reject(error);
        }

        if (error.response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                })
                    .then((token) => {
                        originalRequest.headers['Authorization'] = `Bearer ${token}`;
                        return apiClient(originalRequest);
                    })
                    .catch((err) => Promise.reject(err));
            }

            originalRequest._retry = true;
            isRefreshing = true;

            // Знайти блок оновлення токена всередині apiClient.interceptors.response.use:
            try {
                const authDataStr = localStorage.getItem('auth_tokens');
                if (!authDataStr) throw new Error("No refresh token available");

                const { refresh_token } = JSON.parse(authDataStr);

                const params = new URLSearchParams();
                params.append('grant_type', 'refresh_token');
                params.append('client_id', 'carsharing-client');
                params.append('refresh_token', refresh_token);

                // 🔥 ОБОВ'ЯЗКОВО ДОДАЄМО СЕКРЕТ КЛІЄНТА СЮДИ ТЕЖ!
                const secret = process.env.REACT_APP_KEYCLOAK_SECRET || 'твій_дефолтний_секрет';
                params.append('client_secret', secret);

                const response = await axios.post(KEYCLOAK_TOKEN_URL, params, {
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
                });


                const newTokens = response.data;

                localStorage.setItem('auth_tokens', JSON.stringify(newTokens));

                processQueue(null, newTokens.access_token);
                isRefreshing = false;

                originalRequest.headers['Authorization'] = `Bearer ${newTokens.access_token}`;
                return apiClient(originalRequest);

            } catch (refreshError) {
                processQueue(refreshError, null);
                isRefreshing = false;

                console.error("Сесія прострочена. Потрібен повторний вхід:", refreshError);
                localStorage.removeItem('auth_tokens');
                localStorage.removeItem('user');

                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default apiClient;
