import axios from 'axios';
import apiClient from '../api/apiClient';

const KEYCLOAK_TOKEN_URL = 'http://localhost:8100/realms/carsharing-realm/protocol/openid-connect/token';

const decodeJwtPayload = (token) => {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            window.atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("Помилка декодування JWT:", e);
        return null;
    }
};

export const authService = {
    register: async (userData) => {
        const response = await apiClient.post('/user/v1', userData);
        return response.data;
    },

   login: async (credentials) => {
           const params = new URLSearchParams();
           params.append('client_id', 'carsharing-client');
           params.append('grant_type', 'password');
           params.append('username', credentials.email);
           params.append('password', credentials.password);

           const secret = process.env.REACT_APP_KEYCLOAK_SECRET || '**********';
           params.append('client_secret', secret);

           // 1. Отримуємо токен від Keycloak через Gateway
           const response = await axios.post(KEYCLOAK_TOKEN_URL, params, {
               headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
           });

           if (response.data && response.data.access_token) {
               localStorage.setItem('auth_tokens', JSON.stringify(response.data));

               const payload = decodeJwtPayload(response.data.access_token);
               if (payload) {
                   const roles = payload.realm_access?.roles || [];
                   let determinedRole = 'RENTER';
                   if (roles.includes('ADMINISTRATOR')) determinedRole = 'ADMINISTRATOR';
                   else if (roles.includes('OWNER')) determinedRole = 'OWNER';

                   // 2. Робимо запит до БД для отримання числового ID користувача
                   const keycloakId = payload.sub; // Це UUID на кшталт "30000000-0000..."
                   let dbId = null;

                   try {
                       // Використовуємо apiClient, бо токен вже лежить в localStorage і підставиться в заголовок
                       const userDbResponse = await apiClient.get(`/user/v1/keycloak/${keycloakId}`);
                       if (userDbResponse.data && userDbResponse.data.id) {
                           dbId = userDbResponse.data.id; // Отримуємо наше число (наприклад, 1)
                       }
                   } catch (userErr) {
                       console.error("Не вдалося отримати числовий ID користувача з БД:", userErr);
                   }

                   // 3. Зберігаємо користувача разом з його ЧИСЛОВИМ ID
                   const sessionUser = {
                       id: keycloakId,       // UUID з Keycloak
                       dbId: dbId,           // 🔥 НАШ НОВИЙ ЧИСЛОВИЙ ID З БАЗИ ДАНИХ
                       email: payload.email,
                       fullName: payload.name || `${payload.given_name || ''} ${payload.family_name || ''}`.trim(),
                       role: determinedRole
                   };

                   localStorage.setItem('user', JSON.stringify(sessionUser));
               }
           }
           return response.data;
       },

    isAdmin: () => {
        const userStr = localStorage.getItem('user');
        if (!userStr) return false;
        try {
            const user = JSON.parse(userStr);
            return user.role === 'ADMINISTRATOR';
        } catch {
            return false;
        }
    },

    logout: () => {
        localStorage.removeItem('auth_tokens');
        localStorage.removeItem('user');
    }
};
