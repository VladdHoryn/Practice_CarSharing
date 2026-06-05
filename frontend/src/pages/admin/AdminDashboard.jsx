import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import styles from './AdminDashboard.module.css';

const AdminDashboard = () => {
    const statsCards = [
        { title: 'Всього користувачів', value: '1,248', icon: '👥', color: '#3ba4f6' },
        { title: 'Активні бронювання', value: '42', icon: '🚗', color: '#10b981' },
        { title: 'Дохід за місяць', value: '14,250 €', icon: '🪙', color: '#f59e0b' },
    ];

    const revenueData = [
        { name: 'Січ', revenue: 4000 }, { name: 'Лют', revenue: 6000 },
        { name: 'Бер', revenue: 5500 }, { name: 'Квіт', revenue: 9000 },
        { name: 'Трав', revenue: 14250 },
    ];

    const bookingsData = [
        { name: 'Пн', count: 12 }, { name: 'Вв', count: 19 },
        { name: 'Ср', count: 15 }, { name: 'Чт', count: 22 },
        { name: 'Пт', count: 35 }, { name: 'Сб', count: 42 },
        { name: 'Нд', count: 30 },
    ];

    return (
        <div className={styles.dashboardContainer}>
            <h1 className={styles.title}>Системна аналітика системи платформи</h1>

            <div className={styles.statsGrid}>
                {statsCards.map((card, idx) => (
                    <div key={idx} className={styles.card} style={{ borderLeft: `5px solid ${card.color}` }}>
                        <div className={styles.cardInfo}>
                            <span className={styles.cardTitle}>{card.title}</span>
                            <span className={styles.cardValue}>{card.value}</span>
                        </div>
                        <span className={styles.cardIcon}>{card.icon}</span>
                    </div>
                ))}
            </div>
            <div className={styles.chartsGrid}>
                <div className={styles.chartBox}>
                    <h3>Динаміка доходів (€)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={revenueData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis />
                            <Tooltip />
                            <Area type="monotone" dataKey="revenue" stroke="#3ba4f6" fill="#e2f1fe" />
                        </AreaChart>
                    </ResponsiveContainer>
                </div>

                <div className={styles.chartBox}>
                    <h3>Завантаженість автопарку (Бронювання / день)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={bookingsData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis />
                            <Tooltip />
                            <Bar dataKey="count" fill="#10b981" radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
