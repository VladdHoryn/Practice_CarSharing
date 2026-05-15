import apiClient from '../api/apiClient';

export const carService = {
  // Отримати всі авто
  getAllCars: async () => {
    const response = await apiClient.get('/car/v1');
    return response.data;
  },

  // Отримати всі доступні авто (для Каталогу / головної сторінки "Автопарк")
  getAvailableCars: async () => {
    const response = await apiClient.get('/car/v1/available');
    return response.data;
  },

  // Отримати авто за ID
  getCarById: async (id) => {
    const response = await apiClient.get(`/car/v1/${id}`);
    return response.data;
  },

  // Отримати непідтверджені авто (для кабінету Власника)
  getUnconfirmedCars: async () => {
    const response = await apiClient.get('/car/v1/unconfirmed');
    return response.data;
  }
};
