import React from 'react';
import styles from './AboutAndBlogPage.module.css';

const AboutAndBlogPage = () => {
    const posts = [
        { title: 'Як правильно перевірити авто перед орендою', date: '12 Травня, 2026', readTime: '5 хв', tag: 'Поради' },
        { title: 'Топ-5 місць навколо Чернівців для подорожі на вихідні', date: '28 Квітня, 2026', readTime: '8 хв', tag: 'Подорожі' },
        { title: 'Переваги електромобілів у міському каршерингу', date: '15 Березня, 2026', readTime: '6 хв', tag: 'Тренди' }
    ];

    return (
        <div className={styles.container}>
            {/* Про нас */}
            <section className={styles.aboutHeader}>
                <h1>Про компанію CarLink</h1>
                <p>Ми ламаємо стереотипи про складний та дорогий прокат машин. Наша мета — надати свободу пересування кожному водію в один клік.</p>
                <div className={styles.statsRow}>
                    <div className={styles.statBox}><span>50+</span><p>Авто в парку</p></div>
                    <div className={styles.statBox}><span>10k+</span><p>Задоволених клієнтів</p></div>
                    <div className={styles.statBox}><span>24/7</span><p>Підтримка</p></div>
                </div>
            </section>

            {/* Блог */}
            <section className={styles.blogSection}>
                <h2 className={styles.blogTitle}>Наш авто-блог</h2>
                <div className={styles.blogGrid}>
                    {posts.map((post, idx) => (
                        <article key={idx} className={styles.blogCard}>
                            <div className={styles.tag}>{post.tag}</div>
                            <h3 className={styles.postTitle}>{post.title}</h3>
                            <div className={styles.postMeta}>
                                <span>{post.date}</span>
                                <span>•</span>
                                <span>{post.readTime} читання</span>
                            </div>
                        </article>
                    ))}
                </div>
            </section>
        </div>
    );
};

export default AboutAndBlogPage;
