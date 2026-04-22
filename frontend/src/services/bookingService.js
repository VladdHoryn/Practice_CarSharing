// src/services/bookingService.js
import api from './api';

export const bookingService = {
    createBooking: async (bookingData) => {
        // Очікує дані згідно з твоєю таблицею bookings та split_access
        const response = await api.post('/bookings', bookingData);
        return response.data;
    },

    getUserBookings: async () => {
        const response = await api.get('/bookings/my-history');
        return response.data;
    }
};
