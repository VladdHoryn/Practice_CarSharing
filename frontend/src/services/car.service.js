import apiClient from '../api/apiClient';

export const carService = {
  getAllCars: async () => {
    const response = await apiClient.get('/car/v1');
    return response.data;
  },

  getAvailableCars: async () => {
    // ТИМЧАСОВИЙ ФІКС: Робимо запит до робочого ендпоінту з усіма авто
    const response = await apiClient.get('/car/v1');

    // І самі відфільтровуємо тільки ті, що AVAILABLE
    return response.data.filter(car => car.status === 'AVAILABLE');
  },

  getCarById: async (id) => {
    const response = await apiClient.get(`/car/v1/${id}`);
    return response.data;
  }
};
