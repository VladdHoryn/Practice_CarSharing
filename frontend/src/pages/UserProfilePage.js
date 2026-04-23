import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './UserProfilePage.module.css';

const UserProfilePage = () => {
    const navigate = useNavigate();
    // Стейт для перемикання вкладок
    const [activeTab, setActiveTab] = useState('order');

    // Мокові дані для історії замовлень
    const historyData = [
        // Якщо хочеш побачити пусту таблицю, просто зроби цей масив пустим: []
        { id: 1, car: 'Dacia Logan', period: '12.05.2025 - 15.05.2025', city: 'Київ', price: '87€' }
    ];

    // Меню сайдбару
    const menuItems = [
        { id: 'order', label: 'Замовити авто', icon: '🔑' },
        { id: 'history', label: 'Історія замовлень', icon: '🕒' },
        { id: 'docs', label: 'Документи', icon: '📄' },
        { id: 'profile', label: 'Персональні дані', icon: '👤' },
    ];

    const handleLogout = () => {
        // Тут буде логіка очищення токена (localStorage.removeItem('token'))
        navigate('/login');
    };

    // Функція для рендеру вмісту в залежності від активної вкладки
    const renderTabContent = () => {
        switch (activeTab) {
            case 'order':
                return (
                    <>
                        <h2 className={styles.tabTitle}>Замовити авто</h2>
                        <p className={styles.greeting}>Привіт, макс!</p>
                        <p className={styles.infoText}>
                            Спеціально для вас ми відображаємо статус доступності для всіх авто, щоб процес підбору став для вас ще швидше та зрозуміліше.
                        </p>

                        <p className={styles.greeting}>Ваша персональна знижка на оренду авто становить <span className={styles.discount}>0%</span></p>
                        <p className={styles.infoText}>Для замовлення авто натисніть кнопку нижче.</p>

                        <button className={styles.primaryBtn} onClick={() => navigate('/catalog')}>
                            ЗАБРОНЮВАТИ АВТО
                        </button>
                    </>
                );

            case 'history':
                return (
                    <>
                        <h2 className={styles.tabTitle}>Історія замовлення</h2>
                        <table className={styles.historyTable}>
                            <thead>
                            <tr>
                                <th>Авто</th>
                                <th>Період оренди</th>
                                <th>Місто</th>
                                <th>Ціна</th>
                                <th>Ваш відгук</th>
                                <th>Повтор оренди</th>
                            </tr>
                            </thead>
                            <tbody>
                            {historyData.length > 0 ? (
                                historyData.map(order => (
                                    <tr key={order.id}>
                                        <td><strong>{order.car}</strong></td>
                                        <td>{order.period}</td>
                                        <td>{order.city}</td>
                                        <td><strong>{order.price}</strong></td>
                                        <td><span style={{color: '#3ba4f6', cursor: 'pointer'}}>Залишити</span></td>
                                        <td><span style={{color: '#3ba4f6', cursor: 'pointer'}} onClick={() => navigate('/catalog')}>Повторити</span></td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="6" className={styles.emptyState}>У вас ще немає замовлень.</td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </>
                );

            case 'docs':
                return (
                    <>
                        <h2 className={styles.tabTitle}>Завантажені документи</h2>
                        <p className={styles.greeting} style={{fontSize: '18px'}}>Документи</p>
                        <p className={styles.infoText}>Тут ви можете завантажити зображення документів, що засвідчують вашу особу.</p>

                        <div className={styles.docRow}>
                            <div className={styles.docLabel}>Паспорт 1 та 2 сторінка</div>
                            <input type="file" className={styles.docInput} />
                        </div>
                        <div className={styles.docRow}>
                            <div className={styles.docLabel}>Паспорт прописка</div>
                            <input type="file" className={styles.docInput} />
                        </div>
                        <div className={styles.docRow}>
                            <div className={styles.docLabel}>Водійське посвідчення</div>
                            <input type="file" className={styles.docInput} />
                        </div>

                        <button className={styles.primaryBtn} style={{marginTop: '20px'}}>Надіслати</button>
                    </>
                );

            case 'profile':
                return (
                    <>
                        <h2 className={styles.tabTitle}>Персональні дані</h2>

                        <form onSubmit={(e) => { e.preventDefault(); alert('Дані збережено!'); }}>
                            <div className={styles.formGrid}>
                                <div className={styles.inputGroup}>
                                    <label>Ім'я</label>
                                    <input type="text" defaultValue="Макс" />
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Прізвище</label>
                                    <input type="text" defaultValue="Журик" />
                                </div>
                            </div>

                            <h3 className={styles.sectionSubtitle}>Зміна пароля</h3>
                            <div className={styles.formGrid}>
                                <div className={styles.inputGroup}>
                                    <label>Новий пароль</label>
                                    <input type="password" />
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Повторіть новий пароль</label>
                                    <input type="password" />
                                </div>
                            </div>

                            <button type="submit" className={styles.primaryBtn}>Зберегти зміни</button>
                        </form>
                    </>
                );

            default:
                return null;
        }
    };

    return (
        <div className={styles.pageContainer}>
            <h1 className={styles.mainTitle}>Особистий кабінет</h1>

            <div className={styles.dashboardLayout}>
                {/* Сайдбар */}
                <aside className={styles.sidebar}>
                    <ul className={styles.menuList}>
                        {menuItems.map(item => (
                            <li
                                key={item.id}
                                className={`${styles.menuItem} ${activeTab === item.id ? styles.active : ''}`}
                                onClick={() => setActiveTab(item.id)}
                            >
                                <span className={styles.menuIcon}>{item.icon}</span>
                                {item.label}
                            </li>
                        ))}
                        {/* Кнопка виходу окремо */}
                        <li className={styles.menuItem} onClick={handleLogout} style={{marginTop: '20px', color: '#dc3545'}}>
                            <span className={styles.menuIcon}>🚪</span>
                            Вийти
                        </li>
                    </ul>
                </aside>

                {/* Контентна частина */}
                <main className={styles.contentArea}>
                    {renderTabContent()}
                </main>
            </div>
        </div>
    );
};

export default UserProfilePage;
