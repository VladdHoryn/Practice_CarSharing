// src/services/carService.js
import api from './api';

export const carService = {
    getAllCars: async (filters = {}) => {
        // Можна передавати фільтри як query параметри: ?brand=Dacia&status=AVAILABLE
        const response = await api.get('/cars', { params: filters });
        return response.data;
    },

    getCarById: async (id) => {
        const response = await api.get(`/cars/${id}`);
        return response.data;
    }
};
