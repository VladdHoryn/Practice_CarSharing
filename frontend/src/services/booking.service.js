import apiClient from '../api/apiClient';

export const bookingService = {
  // Створення нового бронювання
  createBooking: async (bookingData) => {
    const response = await apiClient.post('/booking/v1', bookingData);
    return response.data;
  },

  // Отримання списку бронювань конкретного користувача
  getUserBookings: async (userId) => {
    const response = await apiClient.get(`/booking/v1/user/${userId}`);
    return response.data;
  },

  // Скасування бронювання
  cancelBooking: async (id) => {
    const response = await apiClient.post(`/booking/v1/${id}/cancel`);
    return response.data;
  },
};
