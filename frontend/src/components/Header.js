import React from 'react';
import { Link } from 'react-router-dom';
import styles from './Header.module.css';

const Header = () => {
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;
    const isAdmin = user && user.role === 'ADMINISTRATOR';

    return (
        <header className={styles.header}>
            <Link to="/catalog" className={styles.logo}>
                CarLink<span>°</span>
            </Link>

            <nav className={styles.nav}>
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


            <div className={styles.rightSection}>
                <div className={styles.languageSelect}>
                    Виберіть мову:
                    <span className={`${styles.langOption} ${styles.activeLang}`}>ENG</span>
                    <span className={styles.langOption}>UKR</span>
                </div>
                <Link to="/profile" className={styles.userIcon} title={user ? user.fullName : "Профіль"}>
                    👤
                </Link>
            </div>
        </header>
    );
};

export default Header;
