import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { authService } from '../services/auth.service';

const AdminRoute = () => {
    const authTokens = localStorage.getItem('auth_tokens');

    if (!authTokens || !authService.isAdmin()) {
        return <Navigate to="/403" replace />;
    }

    return <Outlet />;
};

export default AdminRoute;
