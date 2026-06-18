import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import axios from 'axios'; // Імпортуємо axios для первинного рефрешу

import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';

import Header from './components/Header';
import Footer from './components/Footer';

import AdminRoute from './components/AdminRoute';
import AdminLayout from './components/AdminLayout';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CarCatalogPage from './pages/CarCatalogPage';
import CarDetailsPage from './pages/CarDetailsPage';
import BookingPage from './pages/BookingPage';
import UserProfilePage from './pages/UserProfilePage';
import HomePage from './pages/HomePage';
import RentalTermsPage from './pages/RentalTermsPage';
import AboutAndBlogPage from './pages/AboutAndBlogPage';
import ContactsPage from './pages/ContactsPage';

import AdminDashboard from './pages/admin/AdminDashboard';
import UserManagement from './pages/admin/UserManagement';
import CarManagement from './pages/admin/CarManagement';
import BookingManagement from './pages/admin/BookingManagement';
import PaymentManagement from './pages/admin/PaymentManagement';
import KycManagement from './pages/admin/KycManagement';
import CarModeration from './pages/admin/CarModeration';

const UserLayout = () => {
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;

    if (user && user.role === 'ADMINISTRATOR') {
        return <Navigate to="/admin/dashboard" replace />;
    }

    return (
        <div className="app-container">
            <Header />
            <main className="main-content">
                <Outlet />
            </main>
            <Footer />
        </div>
    );
};

function App() {

    useEffect(() => {
        const authDataStr = localStorage.getItem('auth_tokens');
        if (authDataStr) {
            try {
                const { refresh_token } = JSON.parse(authDataStr);

                const params = new URLSearchParams();
                params.append('grant_type', 'refresh_token');
                params.append('client_id', 'carsharing-client');
                params.append('refresh_token', refresh_token);


                const secret = process.env.REACT_APP_KEYCLOAK_SECRET || '**********';
                params.append('client_secret', secret);

                axios.post('http://localhost:8100/realms/carsharing-realm/protocol/openid-connect/token', params, {
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
                })
                .then(response => {
                    localStorage.setItem('auth_tokens', JSON.stringify(response.data));
                })
                .catch(() => {
                    localStorage.removeItem('auth_tokens');
                    localStorage.removeItem('user');
                });
            } catch (e) {
                console.error("Помилка парсингу токенів при старті:", e);
            }
        }
    }, []);

    return (
        <Router>
            <Routes>
                <Route element={<UserLayout />}>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/catalog" element={<CarCatalogPage />} />
                    <Route path="/catalog/:id" element={<CarDetailsPage />} />
                    <Route path="/book/:id" element={<BookingPage />} />
                    <Route path="/profile" element={<UserProfilePage />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />

                    <Route path="/terms" element={<RentalTermsPage />} />
                    <Route path="/about" element={<AboutAndBlogPage />} />
                    <Route path="/contacts" element={<ContactsPage />} />
                </Route>

                <Route element={<AdminRoute />}>
                    <Route element={<AdminLayout />}>
                        <Route path="/admin/dashboard" element={<AdminDashboard />} />
                        <Route path="/admin/users" element={<UserManagement />} />
                        <Route path="/admin/fleet" element={<CarManagement />} />
                        <Route path="/admin/bookings" element={<BookingManagement />} />
                        <Route path="/admin/payments" element={<PaymentManagement />} />
                        <Route path="/admin/kyc" element={<KycManagement />} />
                        <Route path="/admin/moderation" element={<CarModeration />} />
                        <Route path="/admin/*" element={<Navigate to="/admin/dashboard" replace />} />
                    </Route>
                </Route>

                <Route path="/403" element={
                    <div style={{ padding: '100px', textAlign: 'center', background: '#f4f6f9', minHeight: '100vh' }}>
                        <h2 style={{ color: '#ef4444', fontSize: '28px' }}>Помилка 403: Доступ обмежено</h2>
                        <p style={{ color: '#64748b' }}>Цей розділ доступний виключно для користувачів із роллю ADMINISTRATOR.</p>
                        <a href="/login" style={{ color: '#3ba4f6', fontWeight: 'bold', textDecoration: 'none' }}>Повернутися до авторизації</a>
                    </div>
                } />
            </Routes>

            <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} />
        </Router>
    );
}

export default App;
