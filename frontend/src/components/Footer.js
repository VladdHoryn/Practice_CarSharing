import React from 'react';
import styles from './Footer.module.css';

const Footer = () => {
    return (
        <footer className={styles.footer}>
            <div className={styles.mainFooter}>

                {/* Логотип */}
                <div className={styles.logo}>
                    CarLink<span>°</span>
                </div>

                {/* Графік роботи */}
                <div className={styles.infoBlock}>
                    <div>
                        <strong>Графік роботи:</strong>
                        Пн-Пт: з 9:00 до 19:00, <strong>Сб-Нд:</strong> з 10:00 до 17:00
                    </div>
                </div>

                {/* Телефон */}
                <div className={styles.phoneBlock}>
                    📱 +38 (xxx)x-xxx-xxx
                </div>

                {/* Соцмережі */}
                <div className={styles.socials}>
                    <a href="/" className={`${styles.socialIcon} ${styles.instagram}`}>📷</a>
                    <a href="/" className={`${styles.socialIcon} ${styles.telegram}`}>✈️</a>
                    <a href="/" className={`${styles.socialIcon} ${styles.viber}`}>📞</a>
                    <a href="/" className={`${styles.socialIcon} ${styles.whatsapp}`}>💬</a>
                </div>

            </div>

            {/* Нижній рядок (Копірайт) */}
            <div className={styles.bottomBar}>
                Car-Link © 2025 – Прокат автомобілів – Stat
            </div>
        </footer>
    );
};

export default Footer;
