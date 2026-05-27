import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Outlet } from 'react-router-dom';

// Компоненти сповіщень та стилі
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';

// Базові компоненти інтерфейсу
import Header from './components/Header';
import Footer from './components/Footer';

// Компоненти захисту та структури адмінки
import AdminRoute from './components/AdminRoute';
import AdminLayout from './components/AdminLayout';

// Публічні та користувацькі сторінки
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CarCatalogPage from './pages/CarCatalogPage';
import CarDetailsPage from './pages/CarDetailsPage';
import BookingPage from './pages/BookingPage';
import UserProfilePage from './pages/UserProfilePage';

// Сторінки адміністратора (Вкладки)
import AdminDashboard from './pages/admin/AdminDashboard';
import UserManagement from './pages/admin/UserManagement';
import CarManagement from './pages/admin/CarManagement';
import BookingManagement from './pages/admin/BookingManagement';
import PaymentManagement from './pages/admin/PaymentManagement';
import KycManagement from './pages/admin/KycManagement';
import CarModeration from './pages/admin/CarModeration';

// Внутрішній макет для звичайних користувачів (щоб ізолювати Header/Footer від адміна)
const UserLayout = () => (
    <div className="app-container">
        <Header />
        <main className="main-content">
            <Outlet /> {/* Сюди рендеряться сторінки користувача */}
        </main>
        <Footer />
    </div>
);

function App() {
    return (
        <Router>
            <Routes>
                {/* =====================================================
                    1. МАРШРУТИ ЗВИЧАЙНИХ КОРИСТУВАЧІВ (З Header та Footer)
                   ===================================================== */}
                <Route element={<UserLayout />}>
                    <Route path="/" element={<Navigate to="/catalog" replace />} />
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/register" element={<RegisterPage />} />
                    <Route path="/catalog" element={<CarCatalogPage />} />
                    <Route path="/catalog/:id" element={<CarDetailsPage />} />
                    <Route path="/book/:id" element={<BookingPage />} />
                    <Route path="/profile" element={<UserProfilePage />} />
                </Route>

                {/* =====================================================
                    2. 🔐 ЗАХИЩЕНІ МАРШРУТИ АДМІНА (Без старого Header/Footer)
                   ===================================================== */}
                <Route element={<AdminRoute />}>
                    <Route element={<AdminLayout />}>
                        <Route path="/admin/dashboard" element={<AdminDashboard />} />
                        <Route path="/admin/users" element={<UserManagement />} />
                        <Route path="/admin/fleet" element={<CarManagement />} />
                        <Route path="/admin/bookings" element={<BookingManagement />} />
                        <Route path="/admin/payments" element={<PaymentManagement />} />
                        <Route path="/admin/kyc" element={<KycManagement />} />
                        <Route path="/admin/moderation" element={<CarModeration />} />
                    </Route>
                </Route>

                {/* Сторінка помилки доступу (Поза всіма макетами) */}
                <Route path="/403" element={
                    <div style={{ padding: '100px', textAlign: 'center', background: '#f4f6f9', minHeight: '100vh' }}>
                        <h2 style={{ color: '#ef4444', fontSize: '28px' }}>Помилка 403: Доступ обмежено</h2>
                        <p style={{ color: '#64748b' }}>Цей розділ доступний виключно для користувачів із роллю ADMINISTRATOR.</p>
                        <a href="/login" style={{ color: '#3ba4f6', fontWeight: 'bold', textDecoration: 'none' }}>Повернутися до авторизації</a>
                    </div>
                } />
            </Routes>

            {/* Глобальний контейнер для красивих тостів */}
            <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} />
        </Router>
    );
}

export default App;
