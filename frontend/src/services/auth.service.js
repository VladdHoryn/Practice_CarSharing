import apiClient from '../api/apiClient';

export const authService = {
    // Авторизація (Логін) через Gateway
    login: async (credentials) => {
        const response = await apiClient.post('/user/v1/login', credentials);

        if (response.data && response.data.user) {
            localStorage.setItem('user', JSON.stringify(response.data.user));
        }
        if (response.data && response.data.token) {
            localStorage.setItem('token', response.data.token);
        }

        return response.data;
    },

    // Реєстрація через Gateway
    register: async (userData) => {
        const response = await apiClient.post('/user/v1/register', userData);
        return response.data;
    },

    // Вихід
    logout: () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
    }
};
