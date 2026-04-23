// src/services/auth.service.js
import apiClient from '../api/apiClient';

export const authService = {
    login: async (credentials) => {
        const response = await apiClient.post('/auth/login', credentials);
        if (response.data.token) {
            localStorage.setItem('token', response.data.token);
        }
        return response.data;
    },

    register: async (userData) => {
        const response = await apiClient.post('/users/register', userData);
        return response.data;
    },

    logout: () => {
        localStorage.removeItem('token');
    }
};
