import apiClient from '../api/apiClient';

export const carService = {
  // Отримати всі авто (йде через шлюз на /car/v1)
  getAllCars: async () => {
    const response = await apiClient.get('/car/v1');
    return response.data;
  },

  // Отримати тільки доступні (йде через шлюз на /car/v1/available)
  getAvailableCars: async () => {
    const response = await apiClient.get('/car/v1/available');
    return response.data;
  },

  // Отримати за ID
  getCarById: async (id) => {
    const response = await apiClient.get(`/car/v1/${id}`);
    return response.data;
  },

  // Отримати непідтверджені
  getUnconfirmedCars: async () => {
    const response = await apiClient.get('/car/v1/unconfirmed');
    return response.data;
  },

  // Створення авто
  createCar: async (carData) => {
    const response = await apiClient.post('/car/v1', carData);
    return response.data;
  },

  // Оновлення авто
  updateCar: async (id, carData) => {
    const response = await apiClient.put(`/car/v1/${id}`, carData);
    return response.data;
  },

  // Видалення авто
  deleteCar: async (id) => {
    const response = await apiClient.delete(`/car/v1/${id}`);
    return response.data;
  }
};
