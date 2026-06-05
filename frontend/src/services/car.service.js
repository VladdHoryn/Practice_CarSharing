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

  getUnconfirmedCars: async () => {
    const response = await apiClient.get('/car/v1/unconfirmed');
    return response.data;
  },

  createCar: async (carData) => {
    const response = await apiClient.post('/car/v1', carData);
    return response.data;
  },

  updateCar: async (id, carData) => {
    const response = await apiClient.put(`/car/v1/${id}`, carData);
    return response.data;
  },

  deleteCar: async (id) => {
    const response = await apiClient.delete(`/car/v1/${id}`);
    return response.data;
  }
};
