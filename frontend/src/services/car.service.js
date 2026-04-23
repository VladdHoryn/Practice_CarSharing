import apiClient from '../api/apiClient';

export const carService = {
    // Отримуємо тільки доступні авто (GET /car/v1/available)
    getAvailableCars: async () => {
        const response = await apiClient.get('/car/v1/available');
        return response.data; // Поверне масив CarResponse
    },

    // Отримати авто за ID (GET /car/v1/{id})
    getCarById: async (id) => {
        const response = await apiClient.get(`/car/v1/${id}`);
        return response.data;
    }
};
