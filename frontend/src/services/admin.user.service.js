import apiClient from '../api/apiClient';

export const adminUserService = {
    getAllUsers: async () => {
        const response = await apiClient.get('/user/v1');
        return response.data;
    },

    activateUser: async (keycloakId) => {
        const response = await apiClient.patch(`/user/v1/${keycloakId}/activate`);
        return response.data;
    },

    deactivateUser: async (keycloakId) => {
        const response = await apiClient.patch(`/user/v1/${keycloakId}/deactivate`);
        return response.data;
    }
};
