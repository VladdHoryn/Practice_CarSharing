import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import styles from './AdminLayout.module.css';

const AdminLayout = () => {
    const navigate = useNavigate();
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    const handleLogout = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        navigate('/login');
    };

    const menuItems = [
        { path: '/admin/dashboard', label: '📊 Дашборд' },
        { path: '/admin/users', label: '👥 Користувачі' },
        { path: '/admin/fleet', label: '🚗 Автопарк' },
        { path: '/admin/bookings', label: '📅 Бронювання' },
        { path: '/admin/payments', label: '💳 Платежі' },
        { path: '/admin/kyc', label: '🛡️ Верифікація (KYC)' },
        { path: '/admin/moderation', label: '🔍 Модерація авто' },
    ];

    return (
        <div className={styles.adminContainer}>
            {/* 1) Спеціальний Header для Адміністратора */}
            <header className={styles.adminHeader}>
                <div className={styles.logo} onClick={() => navigate('/')}>
                    CarLink <span className={styles.badge}>ADMIN</span>
                </div>
                <nav className={styles.navMenu}>
                    {menuItems.map((item) => (
                        <NavLink
                            key={item.path}
                            to={item.path}
                            className={({ isActive }) => isActive ? `${styles.navLink} ${styles.active}` : styles.navLink}
                        >
                            {item.label}
                        </NavLink>
                    ))}
                </nav>
                <div className={styles.adminProfile}>
                    <span>{user.fullName || 'Адміністратор'}</span>
                    <button onClick={handleLogout} className={styles.logoutBtn}>Вихід</button>
                </div>
            </header>

            {/* Контентна область (Сюди рендериться активна вкладка) */}
            <main className={styles.adminMain}>
                <Outlet />
            </main>

            {/* 2) Footer відсутній за твоєю вимогою */}
        </div>
    );
};

export default AdminLayout;
