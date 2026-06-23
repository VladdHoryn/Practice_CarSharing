import React, { useState, useEffect } from 'react';
import { analyticsService } from '../services/analytics.service';

const AdminAnalyticsPage = () => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        analyticsService.getAdminSummary()
            .then(res => setData(res))
            .catch(err => console.error("Помилка глобальної аналітики адміна:", err))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <div style={{ padding: '5px', textAlign: 'center', marginTop: '50px', fontSize: '16px' }}>Агрегація загальносистемних даних платформи... ⏳</div>;
    if (!data) return <div style={{ padding: '20px', textAlign: 'center', color: 'red' }}>Помилка завантаження адмін-панелі аналітики.</div>;

    const ukrDays = { 1.0: 'Пн', 2.0: 'Вт', 3.0: 'Ср', 4.0: 'Чт', 5.0: 'Пт', 6.0: 'Сб', 7.0: 'Нд' };
    const ukrMonths = { 1: 'Січ', 2: 'Лют', 3: 'Бер', 4: 'Квіт', 5: 'Трав', 6: 'Черв', 7: 'Лип', 8: 'Серп', 9: 'Верес', 10: 'Жовт', 11: 'Листоп', 12: 'Груд' };

    const maxAdminRevenue = data.monthlyRevenue?.length > 0 ? Math.max(...data.monthlyRevenue.map(m => m[2])) : 100;
    const maxWeeklyBookings = data.bookingsByDayOfWeek?.length > 0 ? Math.max(...data.bookingsByDayOfWeek.map(d => d[1])) : 1;

    return (
        <div style={{ padding: '30px', background: '#fcfcfd', minHeight: '100vh', fontFamily: 'sans-serif' }}>
            <h1 style={{ fontSize: '26px', color: '#111', marginBottom: '5px', fontWeight: '800' }}>👑 Панель Супер-Адміністратора</h1>
            <p style={{ color: '#666', margin: '0 0 30px 0', fontSize: '14px' }}>Глобальний моніторинг фінансової та користувацької активності системи CarLink</p>

            {}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5, 1fr)', gap: '20px', marginBottom: '35px' }}>
                <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)', border: '1px solid #eee' }}>
                    <div style={{ fontSize: '13px', color: '#888', fontWeight: 'bold' }}>Активні користувачі</div>
                    <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#111', marginTop: '5px' }}>{data.activeUsers} <span style={{fontSize: '14px', color: '#aaa', fontWeight: 'normal'}}>акаунтів</span></div>
                </div>
                <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)', border: '1px solid #eee' }}>
                    <div style={{ fontSize: '13px', color: '#1890ff', fontWeight: 'bold' }}>💼 Власники (OWNER)</div>
                    <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#1890ff', marginTop: '5px' }}>{data.totalOwners}</div>
                </div>
                <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)', border: '1px solid #eee' }}>
                    <div style={{ fontSize: '13px', color: '#722ed1', fontWeight: 'bold' }}>🚗 Орендарі (RENTER)</div>
                    <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#722ed1', marginTop: '5px' }}>{data.totalRenters}</div>
                </div>
                <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)', border: '1px solid #eee' }}>
                    <div style={{ fontSize: '13px', color: '#fa8c16', fontWeight: 'bold' }}>Поточні сесії</div>
                    <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#fa8c16', marginTop: '5px' }}>{data.totalBookings}</div>
                </div>
                <div style={{ background: '#fff', padding: '20px', borderRadius: '10px', boxShadow: '0 2px 8px rgba(0,0,0,0.04)', border: '1px solid #eee' }}>
                    <div style={{ fontSize: '13px', color: '#0050b3', fontWeight: 'bold' }}>Оборот за період</div>
                    <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#0050b3', marginTop: '5px' }}>{data.periodRevenue} €</div>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '30px' }}>
                {}
                <div style={{ background: '#fff', padding: '25px', borderRadius: '12px', border: '1px solid #eee', boxShadow: '0 2px 10px rgba(0,0,0,0.02)' }}>
                    <h3 style={{ margin: '0 0 25px 0', fontSize: '16px', color: '#222' }}>📊 Загальносистемний дохід мікросервісів по місяцях</h3>
                    <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-around', height: '220px', paddingBottom: '25px', borderBottom: '2px solid #f0f0f0' }}>
                        {data.monthlyRevenue?.map(([year, monthNum, revenue], idx) => {
                            const barHeight = (revenue / maxAdminRevenue) * 160;
                            return (
                                <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '80px', position: 'relative' }}>
                                    <span style={{ fontSize: '12px', fontWeight: 'bold', marginBottom: '6px', color: '#52c41a' }}>{revenue}€</span>
                                    <div style={{ height: `${barHeight}px`, width: '40px', background: 'linear-gradient(180deg, #52c41a 0%, #237804 100%)', borderRadius: '4px 4px 0 0' }}></div>
                                    <span style={{ position: 'absolute', bottom: '-24px', fontSize: '12px', color: '#555', fontWeight: 'bold' }}>
                                        {ukrMonths[monthNum]} '{String(year).slice(2)}
                                    </span>
                                </div>
                            );
                        })}
                    </div>
                </div>

                {}
                <div style={{ background: '#fff', padding: '25px', borderRadius: '12px', border: '1px solid #eee', boxShadow: '0 2px 10px rgba(0,0,0,0.02)' }}>
                    <h3 style={{ margin: '0 0 20px 0', fontSize: '16px', color: '#222' }}>📅 Аналітика пікових днів тижня (К-сть оренд)</h3>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '20px' }}>
                        {data.bookingsByDayOfWeek?.map(([dayNum, count], idx) => {
                            const widthPercent = (count / maxWeeklyBookings) * 100;
                            return (
                                <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                    <span style={{ width: '35px', fontSize: '13px', fontWeight: 'bold', color: '#333' }}>{ukrDays[dayNum] || dayNum}</span>
                                    <div style={{ flex: 1, background: '#f5f5f5', height: '18px', borderRadius: '4px', overflow: 'hidden' }}>
                                        <div style={{ width: `${widthPercent}%`, background: 'linear-gradient(90deg, #722ed1 0%, #391085 100%)', height: '100%', borderRadius: '4px', transition: 'width 0.4s ease-in-out' }}></div>
                                    </div>
                                    <span style={{ fontSize: '13px', fontWeight: 'bold', color: '#333', width: '25px', textAlign: 'right' }}>{count}</span>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminAnalyticsPage;
