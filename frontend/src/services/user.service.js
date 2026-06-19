import apiClient from '../api/apiClient';

export const userService = {
    getAllUsers: async () => {
        const response = await apiClient.get('/user/v1');
        return response.data;
    },

    getUserByKeycloakId: async (keycloakId) => {
        const response = await apiClient.get(`/user/v1/keycloak/${keycloakId}`);
        return response.data;
    },

    updateUserByKeycloak: async (keycloakId, userData) => {
        const response = await apiClient.put(`/user/v1/keycloak/${keycloakId}`, userData);
        return response.data;
    },

    deleteUserByKeycloak: async (keycloakId) => {
        const response = await apiClient.delete(`/user/v1/keycloak/${keycloakId}`);
        return response.data;
    },

    activateUserByKeycloak: async (keycloakId) => {
        const response = await apiClient.patch(`/user/v1/${keycloakId}/activate`);
        return response.data;
    },

    deactivateUserByKeycloak: async (keycloakId) => {
        const response = await apiClient.patch(`/user/v1/${keycloakId}/deactivate`);
        return response.data;
    }
};
