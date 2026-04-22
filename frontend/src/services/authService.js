// src/services/authService.js
import api from './api';

export const authService = {
    login: async (credentials) => {
        // Очікує { email, password }
        const response = await api.post('/auth/login', credentials);
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
        }
        return response.data;
    },

    register: async (userData) => {
        // Очікує { email, password, full_name, role, phone }
        const response = await api.post('/auth/register', userData);
        return response.data;
    },

    logout: () => {
        localStorage.removeItem('token');
    }
};
