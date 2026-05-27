import apiClient from '../api/apiClient';

export const userService = {
    getAllUsers: async (params) => {
        // params включає page, size, search
        const response = await apiClient.get('/user/v1/admin/users', { params });
        return response.data;
    },
    updateUserRole: async (id, role) => {
        const response = await apiClient.put(`/user/v1/admin/users/${id}/role`, { role });
        return response.data;
    },
    deleteUser: async (id) => {
        const response = await apiClient.delete(`/user/v1/admin/users/${id}`);
        return response.data;
    }
};
