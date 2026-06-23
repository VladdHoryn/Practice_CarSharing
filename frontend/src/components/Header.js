import React from 'react';
import { Link } from 'react-router-dom';
import styles from './Header.module.css';

import logoImg from '../assets/logo.png';

const Header = () => {
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;
    const isAdmin = user && user.role === 'ADMINISTRATOR';

    return (
        <header className={styles.header} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '15px 40px', background: '#fff', borderBottom: '1px solid #eee' }}>

            {}
            <Link to="/catalog" style={{ display: 'flex', alignItems: 'center', padding: '5px 0', textDecoration: 'none' }}>
                <img
                    src={logoImg}
                    alt="CarLink Logo"
                    style={{
                        height: '52px',
                        width: 'auto',
                        objectFit: 'contain',
                        display: 'block',
                        transition: 'transform 0.2s ease-in-out'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.transform = 'scale(1.03)'}
                    onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
                />
            </Link>

            <nav className={styles.nav} style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
                <Link to="/" className={styles.navLink}>Головна</Link>
                <Link to="/catalog" className={styles.navLink}>Автопарк</Link>

                {isAdmin && (
                    <Link to="/admin/dashboard" className={`${styles.navLink} ${styles.adminLink}`}>
                        Панель адміна
                    </Link>
                )}

                <Link to="/terms" className={styles.navLink}>Умови та Ціни</Link>
                <Link to="/about" className={styles.navLink}>Про нас & Блог</Link>
                <Link to="/contacts" className={styles.navLink}>Контакти</Link>
            </nav>

            <div className={styles.rightSection} style={{ display: 'flex', alignItems: 'center' }}>
                <Link to="/profile" className={styles.userIcon} title={user ? user.fullName : "Профіль"} style={{ fontSize: '20px', textDecoration: 'none' }}>
                    👤
                </Link>
            </div>
        </header>
    );
};

export default Header;
