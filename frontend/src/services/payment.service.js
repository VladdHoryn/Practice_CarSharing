import apiClient from '../api/apiClient';

export const paymentService = {
    createPayment: async (paymentData) => {
        const response = await apiClient.post('/payment/v1', paymentData);
        return response.data;
    },
    getAllPayments: async () => {
        const response = await apiClient.get('/payment/v1');
        return response.data;
    },

    // Бекенд використовує ендпоінт букінгу для зміни статусу платежу, як вказано в ТЗ:
    changePaymentStatus: async (bookingId, newStatus) => {
        const response = await apiClient.post(`/booking/v1/${bookingId}/status/change`, { newStatus });
        return response.data;
    }
};
