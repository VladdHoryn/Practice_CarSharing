import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

// Імпортуємо наші нові компоненти
import Header from './components/Header';
import Footer from './components/Footer';

// Імпортуємо сторінки
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import CarCatalogPage from './pages/CarCatalogPage';
import CarDetailsPage from './pages/CarDetailsPage';
import BookingPage from './pages/BookingPage';
import UserProfilePage from './pages/UserProfilePage';
import './App.css';

function App() {
    return (
        <Router>
            <div className="app-container">

                <Header /> {/* Наша нова шапка */}

                <main className="main-content">
                    <Routes>
                        <Route path="/" element={<Navigate to="/catalog" replace />} />
                        <Route path="/login" element={<LoginPage />} />
                        <Route path="/register" element={<RegisterPage />} />
                        <Route path="/catalog" element={<CarCatalogPage />} />
                        <Route path="/catalog/:id" element={<CarDetailsPage />} />
                        <Route path="/book/:id" element={<BookingPage />} />
                        <Route path="/profile" element={<UserProfilePage />} />
                    </Routes>
                </main>

                <Footer /> {/* Наш новий підвал */}

            </div>
        </Router>
    );
}

export default App;
