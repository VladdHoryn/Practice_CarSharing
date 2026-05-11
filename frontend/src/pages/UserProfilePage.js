import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './UserProfilePage.module.css';
import { authService } from '../services/auth.service';

const UserProfilePage = () => {
    const navigate = useNavigate();
    const [activeTab, setActiveTab] = useState('order');

    // Стейт для реальних даних користувача
    const [user, setUser] = useState({
        id: '',
        firstName: '',
        lastName: '',
        email: '',
        role: ''
    });

    // При завантаженні сторінки перевіряємо, чи є юзер
    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);

            // Розбиваємо fullName на Ім'я та Прізвище
            const nameParts = parsedUser.fullName ? parsedUser.fullName.split(' ') : [''];
            const firstName = nameParts[0] || '';
            const lastName = nameParts.slice(1).join(' ') || '';

            setUser({
                id: parsedUser.id,
                firstName: firstName,
                lastName: lastName,
                email: parsedUser.email,
                role: parsedUser.role
            });
        } else {
            // Якщо користувач не залогінений - викидаємо на сторінку логіну
            navigate('/login');
        }
    }, [navigate]);

    // Мокові дані для історії замовлень (поки не підключили booking-service)
    const historyData = [
        { id: 1, car: 'Dacia Logan', period: '12.05.2025 - 15.05.2025', city: 'Київ', price: '87€' }
    ];

    const menuItems = [
        { id: 'order', label: 'Замовити авто', icon: '🔑' },
        { id: 'history', label: 'Історія замовлень', icon: '🕒' },
        { id: 'docs', label: 'Документи', icon: '📄' },
        { id: 'profile', label: 'Персональні дані', icon: '👤' },
    ];

    const handleLogout = () => {
        authService.logout(); // Очищаємо localStorage
        navigate('/login');   // Перекидаємо на логін
    };

    // Обробник зміни даних у формі профілю
    const handleProfileChange = (e) => {
        setUser({ ...user, [e.target.name]: e.target.value });
    };

    const handleProfileSubmit = (e) => {
        e.preventDefault();
        // Тут в майбутньому буде запит до бекенду на оновлення даних: axios.put(`/user/v1/${user.id}`, ...)
        alert('Ця функція скоро запрацює! Ваші нові дані: ' + user.firstName + ' ' + user.lastName);
    };

    const renderTabContent = () => {
        switch (activeTab) {
            case 'order':
                return (
                    <>
                        <h2 className={styles.tabTitle}>Замовити авто</h2>
                        {/* Підставляємо реальне ім'я */}
                        <p className={styles.greeting}>Привіт, {user.firstName || 'Гість'}!</p>
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

                        <form onSubmit={handleProfileSubmit}>
                            <div className={styles.formGrid}>
                                <div className={styles.inputGroup}>
                                    <label>Ім'я</label>
                                    <input
                                        type="text"
                                        name="firstName"
                                        value={user.firstName}
                                        onChange={handleProfileChange}
                                    />
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Прізвище</label>
                                    <input
                                        type="text"
                                        name="lastName"
                                        value={user.lastName}
                                        onChange={handleProfileChange}
                                    />
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Email (не змінюється)</label>
                                    <input
                                        type="email"
                                        value={user.email}
                                        disabled
                                        style={{backgroundColor: '#f5f5f5'}}
                                    />
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Ваша роль</label>
                                    <input
                                        type="text"
                                        value={user.role === 'OWNER' ? 'Орендодавець' : 'Орендар'}
                                        disabled
                                        style={{backgroundColor: '#f5f5f5'}}
                                    />
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
