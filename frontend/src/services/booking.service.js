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

  getBookingById: async (id) => {
    const response = await apiClient.get(`/booking/v1/${id}`);
    return response.data;
  },

  cancelBooking: async (id) => {
    const response = await apiClient.post(`/booking/v1/${id}/cancel`);
    return response.data;
  },

  getAllBookings: async () => {
    const response = await apiClient.get('/booking/v1');
    return response.data;
  },

  changeBookingStatus: async (id, newStatus) => {
    const response = await apiClient.post(`/booking/v1/${id}/status/change`, { newStatus });
    return response.data;
  },

  getAllInvitations: async () => {
    const response = await apiClient.get('/booking/v1/drivers');
    return response.data;
  },

  getInvitationsByUserId: async (userId) => {
    const response = await apiClient.get(`/booking/v1/drivers/${userId}`);
    return response.data;
  },

  createInvitation: async (bookingId, driverData) => {
    const response = await apiClient.post(`/booking/v1/${bookingId}/drivers`, driverData);
    return response.data;
  },

  acceptInvitation: async (invitationId) => {
    const response = await apiClient.post(`/booking/v1/drivers/${invitationId}/accept`);
    return response.data;
  },

  declineInvitation: async (invitationId) => {
    const response = await apiClient.post(`/booking/v1/drivers/${invitationId}/decline`);
    return response.data;
  }
};
