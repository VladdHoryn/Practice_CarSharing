import apiClient from '../api/apiClient';

const BASE_URL = '/document/v1';

export const documentService = {
    uploadDocument: async (userId, documentType, file) => {
        const formData = new FormData();
        formData.append('documentType', documentType);
        formData.append('file', file);
        const response = await apiClient.post(`${BASE_URL}/user/${userId}`, formData);
        return response.data;
    },

    getMetadata: async (userId) => {
        const response = await apiClient.get(`${BASE_URL}/user/${userId}`);
        return response.data;
    },

    getProfileStatus: async (userId) => {
        const response = await apiClient.get(`${BASE_URL}/user/${userId}/status`);
        return response.data;
    },

    verifyDocument: async (documentId) => {
        const response = await apiClient.patch(`${BASE_URL}/${documentId}/verify`);
        return response.data;
    }
};
