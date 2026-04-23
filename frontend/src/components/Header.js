import React from 'react';
import { Link } from 'react-router-dom';
import styles from './Header.module.css';

const Header = () => {
    return (
        <header className={styles.header}>
            {/* Логотип */}
            <Link to="/catalog" className={styles.logo}>
                CarLink<span>°</span>
            </Link>

            {/* Навігація */}
            <nav className={styles.nav}>
                <Link to="/" className={styles.navLink}>Головна</Link>
                <Link to="/catalog" className={styles.navLink}>Автопарк</Link>
                <Link to="/" className={styles.navLink}>Ціни</Link>
                <Link to="/" className={styles.navLink}>Про нас</Link>
                <Link to="/" className={styles.navLink}>Відгуки клієнтів</Link>
                <Link to="/" className={styles.navLink}>Умови оренди</Link>
                <Link to="/" className={styles.navLink}>Наш блог</Link>
                <Link to="/" className={styles.navLink}>Контакти</Link>
            </nav>

            {/* Права секція (Мова + Профіль) */}
            <div className={styles.rightSection}>
                <div className={styles.languageSelect}>
                    Виберіть мову:
                    <span className={`${styles.langOption} ${styles.activeLang}`}>ENG</span>
                    <span className={styles.langOption}>UKR</span>
                </div>
                <Link to="/profile" className={styles.userIcon}>
                    {/* Проста іконка користувача (можна замінити на SVG) */}
                    👤
                </Link>
            </div>
        </header>
    );
};

export default Header;
