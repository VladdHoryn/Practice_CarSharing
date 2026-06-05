import apiClient from '../api/apiClient';

export const authService = {
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
    isAdmin: () => {
            const userStr = localStorage.getItem('user');
            if (!userStr) return false;
            try {
                const user = JSON.parse(userStr);
                return user.role === 'ADMINISTRATOR';
            } catch {
                return false;
            }
        },

    register: async (userData) => {
        const response = await apiClient.post('/user/v1/register', userData);
        return response.data;
    },

    logout: () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
    }
};
