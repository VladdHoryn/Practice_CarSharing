import React from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './HomePage.module.css';

const HomePage = () => {
    const navigate = useNavigate();

    const categories = [
        { title: 'Економ', desc: 'Для міських поїздок', price: 'від 20€', img: '🏙️' },
        { title: 'Комфорт', desc: 'Для бізнесу та подорожей', price: 'від 40€', img: '💼' },
        { title: 'Електро', desc: 'Екологічність та технології', price: 'від 50€', img: '⚡' },
        { title: 'Позашляховики', desc: 'Для будь-яких доріг', price: 'від 70€', img: '⛰️' },
    ];

    return (
        <div className={styles.wrapper}>
            {/* Hero Section */}
            <section className={styles.hero}>
                <div className={styles.heroOverlay}>
                    <h1 className={styles.heroTitle}>Швидка оренда авто в Чернівцях</h1>
                    <p className={styles.heroSubtitle}>Ідеальний технічний стан, прозорі умови та подача за 15 хвилин.</p>
                    <button onClick={() => navigate('/catalog')} className={styles.ctaBtn}>
                        Обрати автомобіль ➔
                    </button>
                </div>
            </section>

            {/* Переваги */}
            <section className={styles.features}>
                <h2 className={styles.sectionTitle}>Чому обирають CarLink<span>°</span></h2>
                <div className={styles.featuresGrid}>
                    <div className={styles.featureCard}>
                        <div className={styles.iconBox}>🛡️</div>
                        <h3>Повне КАСКО</h3>
                        <p>Усі автомобілі застраховані. Ваш спокій — наш головний пріоритет.</p>
                    </div>
                    <div className={styles.featureCard}>
                        <div className={styles.iconBox}>⚡</div>
                        <h3>Швидке оформлення</h3>
                        <p>Всього 2 документи, 5 хвилин на верифікацію — і ключі у вас.</p>
                    </div>
                    <div className={styles.featureCard}>
                        <div className={styles.iconBox}>🧼</div>
                        <h3>Ідеальна чистота</h3>
                        <p>Кожне авто проходить комплексну мийку та дезінфекцію перед видачею.</p>
                    </div>
                </div>
            </section>

            {/* Категорії авто */}
            <section className={styles.categoriesSection}>
                <h2 className={styles.sectionTitle}>Наш автопарк</h2>
                <div className={styles.categoriesGrid}>
                    {categories.map((cat, idx) => (
                        <div key={idx} className={styles.catCard} onClick={() => navigate('/catalog')}>
                            <div className={styles.catIcon}>{cat.img}</div>
                            <h3>{cat.title}</h3>
                            <p>{cat.desc}</p>
                            <span className={styles.catPrice}>{cat.price}</span>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
};

export default HomePage;
