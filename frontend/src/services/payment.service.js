import axios from 'axios';

const PAYMENT_API_URL = 'http://localhost:8084/payment/v1';

export const paymentService = {
    createPayment: async (paymentData) => {
        const response = await axios.post(PAYMENT_API_URL, paymentData);
        return response.data;
    }
};
