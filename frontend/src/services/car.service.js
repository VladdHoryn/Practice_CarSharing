import axios from 'axios';
import apiClient from '../api/apiClient';

export const carService = {
  // Отримати всі авто
  getAllCars: async () => {
    const response = await apiClient.get('/car/v1');
    return response.data;
  },
  // Отримати всі доступні авто з фото
  getAvailableCars: async () => {
      try {
          const response = await apiClient.get('/car/v1/available');
          return response.data;
      } catch (error) {
          console.error('Error fetching available cars:', error);
          throw error;
      }
  },

  // Отримати авто за ID
  getCarById: async (id) => {
      try {
          const response = await apiClient.get(`/car/v1/${id}`);
          return response.data;
      } catch (error) {
          console.error(`Error fetching car ${id}:`, error);
          throw error;
      }
  },

  // Отримати всі фото авто
  getCarImages: async (id) => {
      try {
          const response = await apiClient.get(`/car/v1/${id}/images`);
          return response.data;
      } catch (error) {
          console.error(`Error fetching images for car ${id}:`, error);
          throw error;
      }
  },

  // Отримати головне фото
  getCarMainImage: async (id) => {
      try {
          const response = await apiClient.get(`/car/v1/${id}/main-image`);
          return response.data;
      } catch (error) {
          console.error(`Error fetching main image for car ${id}:`, error);
          throw error;
      }
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
