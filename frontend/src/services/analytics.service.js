import apiClient from '../api/apiClient'; // 👑 ФІКС: використовуємо наш єдиний авторизований клієнт

export const analyticsService = {
    getOwnerSummary: async (ownerId) => {
        // apiClient автоматично додасть headers: { Authorization: 'Bearer <токен>' }
        const response = await apiClient.get(`/api/v1/analytics/owners/${ownerId}/summary`, {
            params: {
                completedStatus: 'COMPLETED',
                activeStatuses: 'PENDING,CONFIRMED',
                yearStart: '2026-01-01T00:00:00',
                weekStart: '2026-06-10T00:00:00',
                weekEnd: '2026-06-16T23:59:59'
            }
        });
        return response.data;
    },

    // 👑 Збір аналітики для ADMINISTRATOR
    getAdminSummary: async () => {
        const response = await apiClient.get('/api/v1/analytics/admin/summary', {
            params: {
                periodStart: '2024-05-01T00:00:00',
                upcomingStart: '2024-06-17T00:00:00',
                upcomingEnd: '2024-07-17T23:59:59'
            }
        });
        return response.data;
    }
};
