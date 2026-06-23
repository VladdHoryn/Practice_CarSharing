import apiClient from '../api/apiClient';

export const carService = {
    getAllCars: async () => {
        const response = await apiClient.get('/car/v1');
        return response.data;
    },

    getAvailableCars: async () => {
        const response = await apiClient.get('/car/v1/available');
        return response.data;
    },

    getCarById: async (id) => {
        const response = await apiClient.get(`/car/v1/${id}`);
        return response.data;
    },

    createCar: async (carData) => {
        const response = await apiClient.post('/car/v1', carData);
        return response.data;
    },

    updateCar: async (id, carData) => {
        const response = await apiClient.put(`/car/v1/${id}`, carData);
        return response.data;
    },

    deleteCar: async (id) => {
        const response = await apiClient.delete(`/car/v1/${id}`);
        return response.data;
    },

    changeCarStatus: async (id, newStatus) => {
        const response = await apiClient.post(`/car/v1/${id}/status/change`, { newStatus });
        return response.data;
    },

    getCarImages: async (carId) => {
        const response = await apiClient.get(`/car/v1/${carId}/images`);
        return response.data;
    },

    getUnconfirmedCars: async () => {
        const response = await apiClient.get('/car/v1/unconfirmed');
        return response.data;
    },

    confirmModeration: async (id) => {
        const response = await apiClient.post(`/car/v1/${id}/moderation/confirm`);
        return response.data;
    },

    uploadCarImage: async (carId, file) => {
        const formData = new FormData();
        formData.append('file', file);

        // 👑 ФІКС: Видалили об'єкт headers, тепер завантаження фотографій машин не буде падати
        const response = await apiClient.post(`/car/v1/${carId}/images`, formData);
        return response.data;
    },

    cancelModeration: async (id) => {
        const response = await apiClient.post(`/car/v1/${id}/moderation/cancel`);
        return response.data;
    },

    getCarsByOwnerId: async (ownerId) => {
        const response = await apiClient.get(`/car/v1/owner/${ownerId}`);
        return response.data;
    }
};
