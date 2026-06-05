import apiClient from '../api/apiClient';

export const paymentService = {
    createPayment: async (paymentData) => {
        const response = await apiClient.post('/payment/v1', paymentData);
        return response.data;
    }
};
