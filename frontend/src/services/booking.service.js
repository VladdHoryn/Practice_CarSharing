import axios from 'axios';

// Зверни увагу: порт 8082, як вказав бекендер
const BOOKING_API_URL = 'http://localhost:8082/booking/v1';

export const bookingService = {
  // Створення нового бронювання
  createBooking: async (bookingData) => {
    const response = await axios.post(BOOKING_API_URL, bookingData);
    return response.data;
  },

  // Отримання списку бронювань конкретного користувача
  getUserBookings: async (userId) => {
    const response = await axios.get(`${BOOKING_API_URL}/user/${userId}`);
    return response.data;
  },

  // Скасування бронювання
  cancelBooking: async (id) => {
    const response = await axios.post(`${BOOKING_API_URL}/${id}/cancel`);
    return response.data;
  },
};
