import React, { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area } from 'recharts';
import styles from './AdminDashboard.module.css';
import { analyticsService } from '../../services/analytics.service';

const AdminDashboard = () => {
    const [adminData, setAdminData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchAdminAnalytics = async () => {
            try {
                setLoading(true);
                const data = await analyticsService.getAdminSummary();
                setAdminData(data);
            } catch (err) {
                console.error("Критична помилка завантаження адмін-дашборду:", err);
            } finally {
                setLoading(false);
            }
        };
        fetchAdminAnalytics();
    }, []);

    if (loading) return <div style={{ padding: '100px', textAlign: 'center', fontSize: '16px' }}>Агрегація загальносистемних даних платформи... ⏳</div>;
    if (!adminData) return <div style={{ padding: '100px', textAlign: 'center', color: 'red' }}>Не вдалося завантажити глобальну аналітику.</div>;


    const ukrMonths = { 1: 'Січ', 2: 'Лют', 3: 'Бер', 4: 'Квіт', 5: 'Трав', 6: 'Черв', 7: 'Лип', 8: 'Серп', 9: 'Верес', 10: 'Жовт', 11: 'Листоп', 12: 'Груд' };
    const ukrDays = { 1.0: 'Пн', 2.0: 'Вв', 3.0: 'Ср', 4.0: 'Чт', 5.0: 'Пт', 6.0: 'Сб', 7.0: 'Нд' };


    const statsCards = [
        { title: 'Активні користувачі', value: adminData.activeUsers, icon: '👥', color: '#3ba4f6' },
        { title: 'Власники (OWNER)', value: adminData.totalOwners, icon: '💼', color: '#722ed1' },
        { title: 'Орендарі (RENTER)', value: adminData.totalRenters, icon: '🚗', color: '#10b981' },
        { title: 'Глобальний обіг', value: `${adminData.periodRevenue} €`, icon: '🪙', color: '#f59e0b' },
    ];

    const revenueData = adminData.monthlyRevenue?.map(([year, month, amount]) => ({
        name: `${ukrMonths[month]} '${String(year).slice(2)}`,
        revenue: amount
    })) || [];


    const bookingsData = adminData.bookingsByDayOfWeek?.map(([dayNum, count]) => ({
        name: ukrDays[dayNum] || `Дн ${dayNum}`,
        count: count
    })) || [];

    return (
        <div className={styles.dashboardContainer}>
            <h1 className={styles.title}>Системна аналітика платформи CarLink</h1>


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

            {}
            <div className={styles.chartsGrid}>
                <div className={styles.chartBox}>
                    <h3>Динаміка доходів системи (€)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <AreaChart data={revenueData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis />
                            <Tooltip formatter={(value) => [`${value} €`, 'Дохід']} />
                            <Area type="monotone" dataKey="revenue" stroke="#3ba4f6" fill="#e2f1fe" />
                        </AreaChart>
                    </ResponsiveContainer>
                </div>

                <div className={styles.chartBox}>
                    <h3>Завантаженість автопарку платформи (Бронювання / день тижня)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={bookingsData}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="name" />
                            <YAxis />
                            <Tooltip formatter={(value) => [value, 'Кількість оренд']} />
                            <Bar dataKey="count" fill="#10b981" radius={[4, 4, 0, 0]} />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
