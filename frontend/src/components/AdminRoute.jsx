import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { authService } from '../services/auth.service';

const AdminRoute = () => {
    return authService.isAdmin() ? <Outlet /> : <Navigate to="/403" replace />;
};

export default AdminRoute;
