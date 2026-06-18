import React from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import styles from './AdminLayout.module.css';

const AdminLayout = () => {
    const navigate = useNavigate();
    const user = JSON.parse(localStorage.getItem('user') || '{}');

    const handleLogout = () => {
        localStorage.removeItem('user');
        localStorage.removeItem('auth_tokens');
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
            <header className={styles.adminHeader}>
                <div className={styles.logoStatic}>
                    CarLink<span>°</span> <span className={styles.badge}>ADMIN PANEL</span>
                </div>

                <div className={styles.adminProfile}>
                    <span className={styles.adminName}>👤 {user.fullName || 'Адміністратор'}</span>
                    <button onClick={handleLogout} className={styles.logoutBtn}>Вихід</button>
                </div>
            </header>

            <div className={styles.adminWorkspace}>
                <aside className={styles.sidebar}>
                    <nav className={styles.sidebarNav}>
                        {menuItems.map((item) => (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                className={({ isActive }) => isActive ? `${styles.sidebarLink} ${styles.activeLink}` : styles.sidebarLink}
                            >
                                {item.label}
                            </NavLink>
                        ))}
                    </nav>
                </aside>

                <main className={styles.adminMain}>
                    <Outlet />
                </main>
            </div>
        </div>
    );
};

export default AdminLayout;
