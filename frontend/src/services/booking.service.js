import apiClient from '../api/apiClient';

export const bookingService = {
  createBooking: async (bookingData) => {
    const response = await apiClient.post('/booking/v1', bookingData);
    return response.data;
  },

  getUserBookings: async (userId) => {
    const response = await apiClient.get(`/booking/v1/user/${userId}`);
    return response.data;
  },

  cancelBooking: async (id) => {
    const response = await apiClient.post(`/booking/v1/${id}/cancel`);
    return response.data;
  },
};
