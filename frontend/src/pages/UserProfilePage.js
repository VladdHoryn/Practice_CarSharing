import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './UserProfilePage.module.css';
import { authService } from '../services/auth.service';
import { bookingService } from '../services/booking.service';

// --- SVG ІКОНКИ ДЛЯ МЕНЮ ---
const Icons = {
    order: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"></path></svg>,
    history: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>,
    docs: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>,
    profile: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>,
    fleet: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path><circle cx="7" cy="17" r="2"></circle><path d="M9 17h6"></path><circle cx="17" cy="17" r="2"></circle></svg>,
    analytics: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>,
    logout: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
};

const UserProfilePage = () => {
    const navigate = useNavigate();

    const [user, setUser] = useState({
        id: '',
        firstName: '',
        lastName: '',
        email: '',
        role: '' // 'RENTER' або 'OWNER'
    });

    const [activeTab, setActiveTab] = useState('');

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            const nameParts = parsedUser.fullName ? parsedUser.fullName.split(' ') : [''];

            setUser({
                id: parsedUser.id,
                firstName: nameParts[0] || '',
                lastName: nameParts.slice(1).join(' ') || '',
                email: parsedUser.email,
                role: parsedUser.role
            });

            if (parsedUser.role === 'OWNER') {
                setActiveTab('fleet');
            } else {
                setActiveTab('order');
            }
        } else {
            navigate('/login');
        }
    }, [navigate]);

    const [bookings, setBookings] = useState([]);

    const menuItems = [
        { id: 'order', label: 'Замовити авто', icon: '🔑' },
        { id: 'history', label: 'Історія замовлень', icon: '🕒' },
        { id: 'docs', label: 'Документи', icon: '📄' },
        { id: 'profile', label: 'Персональні дані', icon: '👤' },
    ];

    const handleLogout = () => {
        authService.logout();
        navigate('/login');
    };

    const renterMenu = [
        { id: 'order', label: 'Замовити авто', icon: Icons.order },
        { id: 'history', label: 'Історія замовлень', icon: Icons.history },
        { id: 'docs', label: 'Документи', icon: Icons.docs },
        { id: 'profile', label: 'Персональні дані', icon: Icons.profile },
    ];

    const ownerMenu = [
        { id: 'fleet', label: 'Автопарк', icon: Icons.fleet },
        { id: 'analytics', label: 'Аналітика', icon: Icons.analytics },
        { id: 'profile', label: 'Персональні дані', icon: Icons.profile },
    ];

    const menuItems = user.role === 'OWNER' ? ownerMenu : renterMenu;

    const mockFleet = [
        { id: 1, photo: 'https://via.placeholder.com/60x40', model: 'Toyota Camry', vin: 'VXXXXXXXXX', year: 2022, mileage: 'Пробіг', status: '✔️', price: '1200 грн/добу' },
        { id: 2, photo: 'https://via.placeholder.com/60x40', model: 'Toyota Camry', vin: 'VXXXXXXXXX', year: 2022, mileage: 'Пробіг', status: '✔️', price: '1200 грн/добу' }
    ];

    const mockAnalyticsDays = [
        { day: 'Пн', value: 70 }, { day: 'Вв', value: 75 }, { day: 'Ср', value: 60 },
        { day: 'Чт', value: 45 }, { day: 'Пт', value: 75 }, { day: 'Сб', value: 95 }, { day: 'Нд', value: 80 }
    ];
    const handleProfileSubmit = (e) => {
        e.preventDefault();
        // Тут в майбутньому буде запит до бекенду на оновлення даних: axios.put(`/user/v1/${user.id}`, ...)
        alert('Ця функція скоро запрацює! Ваші нові дані: ' + user.firstName + ' ' + user.lastName);
    };
    // 2. Завантаження бронювань при переході на вкладку history
    useEffect(() => {
        if (activeTab === 'history' && user.id) {
            bookingService.getUserBookings(user.id)
                .then(data => setBookings(data))
                .catch(err => console.error("Помилка завантаження бронювань:", err));
        }
    }, [activeTab, user.id]);

    const renderTabContent = () => {
        // --- Вкладки ВЛАСНИКА (OWNER) ---
        if (activeTab === 'fleet') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Керування автопарком (Список)</h2>
                    <button className={styles.addCarBtn}>+ ДОДАТИ АВТО</button>

                    <div className={styles.filtersRow}>
                        <div className={styles.inputGroup}>
                            <label>Марка</label>
                            <select><option>all</option></select>
                        </div>
                        <div className={styles.inputGroup}>
                            <label>Статус</label>
                            <select><option>all</option></select>
                        </div>
                        <div className={styles.inputGroup}>
                            <label>Рік</label>
                            <select><option>all</option></select>
                        </div>
                        <div className={styles.inputGroup}>
                            <label>VIN</label>
                            <input type="text" placeholder="Пошук за VIN" />
                        </div>
                    </div>

                    <table className={styles.historyTable}>
                        <thead>
                        <tr>
                            <th>Фото</th>
                            <th>Модель</th>
                            <th>VIN</th>
                            <th>Рік</th>
                            <th>Пробіг</th>
                            <th>Статус</th>
                            <th>Ціна</th>
                            <th>Дії</th>
                        </tr>
                        </thead>
                        <tbody>
                        {mockFleet.map(car => (
                            <tr key={car.id}>
                                <td><img src={car.photo} alt="car" className={styles.carThumb}/></td>
                                <td>{car.model}</td>
                                <td>{car.vin}</td>
                                <td>{car.year}</td>
                                <td>{car.mileage}</td>
                                <td>{car.status}</td>
                                <td>{car.price}</td>
                                <td><span style={{color: '#3ba4f6', cursor:'pointer'}}>Редаг.</span></td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </>
            );
        }

        if (activeTab === 'analytics') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Аналітика та звіти</h2>

                    <div className={styles.statsContainer}>
                        <div className={styles.statCard}>
                            <div className={styles.statCardTitle}>Кількість бронювань</div>
                            <div className={styles.statCardValue}>150</div>
                        </div>
                        <div className={styles.statCard}>
                            <div className={styles.statCardTitle}>Загальна виручка</div>
                            <div className={styles.statCardValue}>250 000 грн</div>
                        </div>
                        <div className={styles.statCard}>
                            <div className={styles.statCardTitle}>Рівень завантаженості</div>
                            <div className={styles.statCardValue}>75%</div>
                        </div>
                        <div className={styles.statCard}>
                            <div className={styles.statCardTitle}>Кількість авто</div>
                            <div className={styles.statCardValue}>5</div>
                        </div>
                    </div>

                    <div className={styles.chartsGrid}>
                        <div className={styles.chartBox}>
                            <div className={styles.chartTitle}>Виручка за місяць</div>
                            <div className={styles.cssBarChart}>
                                {mockAnalyticsDays.map((item, i) => (
                                    <div key={i} className={styles.barCol}>
                                        <div className={styles.barFill} style={{ height: `${item.value - 20}%` }}></div>
                                        <span className={styles.barLabel}>Міс {i+1}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                        <div className={styles.chartBox}>
                            <div className={styles.chartTitle}>Завантаженість по днях тижня</div>
                            <div className={styles.cssBarChart}>
                                {mockAnalyticsDays.map((item, i) => (
                                    <div key={i} className={styles.barCol}>
                                        <div className={styles.barFill} style={{ height: `${item.value}%` }}></div>
                                        <span className={styles.barLabel}>{item.day}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    <table className={styles.historyTable}>
                        <thead>
                        <tr>
                            <th>Фото</th>
                            <th>Модель</th>
                            <th>Стан</th>
                            <th>Дохід</th>
                            <th>Кількість бронювань</th>
                        </tr>
                        </thead>
                        <tbody>
                        {mockFleet.map(car => (
                            <tr key={car.id}>
                                <td><img src={car.photo} alt="car" className={styles.carThumb}/></td>
                                <td>{car.model}</td>
                                <td>{car.status}</td>
                                <td><strong>20 000 грн</strong></td>
                                <td>5</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </>
            );
        }

        // --- Вкладки ОРЕНДАРЯ (RENTER) ---
        if (activeTab === 'order') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Замовити авто</h2>
                    <p className={styles.greeting}>Привіт, {user.firstName || 'Гість'}!</p>
                    <p className={styles.infoText}>Спеціально для вас ми відображаємо статус доступності для всіх авто, щоб процес підбору став для вас ще швидше та зрозуміліше.</p>
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
                                <th>ID Бронювання</th>
                                <th>ID Авто</th>
                                <th>Період</th>
                                <th>Статус</th>
                                <th>Сума</th>
                            </tr>
                            </thead>
                            <tbody>
                            {bookings.length > 0 ? (
                                bookings.map(order => (
                                    <tr key={order.id}>
                                        <td>#{order.id}</td>
                                        <td><strong>Авто ID: {order.carId}</strong></td>
                                        <td>{order.startDate.split('T')[0]} — {order.endDate.split('T')[0]}</td>
                                        <td>
                                            <span className={styles.statusBadge} style={{
                                                color: order.status === 'CREATED' ? '#28a745' : '#666'
                                            }}>
                                                {order.status}
                                            </span>
                                        </td>
                                        <td><strong>{order.totalPrice}€</strong></td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="5" className={styles.emptyState}>У вас ще немає замовлень.</td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </>
                );

                    <p className={styles.greeting}>Ваша персональна знижка на оренду авто становить <span className={styles.discount}>0%</span></p>
                    <p className={styles.infoText}>Для замовлення авто натисніть кнопку нижче.</p>
                    <button className={styles.primaryBtn} onClick={() => navigate('/catalog')}>ЗАБРОНЮВАТИ АВТО</button>
                </>
            );
        }

        if (activeTab === 'history') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Історія замовлень</h2>
                    <table className={styles.historyTable}>
                        <thead><tr><th>Авто</th><th>Період оренди</th><th>Місто</th><th>Ціна</th><th>Ваш відгук</th><th>Повтор оренди</th></tr></thead>
                        <tbody><tr><td colSpan="6" className={styles.emptyState}>У вас ще немає замовлень.</td></tr></tbody>
                    </table>
                </>
            );
        }

        if (activeTab === 'docs') {
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
        }

        // --- СПІЛЬНА ВКЛАДКА: ПЕРСОНАЛЬНІ ДАНІ ---
        if (activeTab === 'profile') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Персональні дані</h2>
                    <form>
                        <div className={styles.formGrid}>
                            <div className={styles.inputGroup}>
                                <label>Ім'я</label>
                                <input type="text" defaultValue={user.firstName} />
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Прізвище</label>
                                <input type="text" defaultValue={user.lastName} />
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Email (не змінюється)</label>
                                <input type="email" value={user.email} disabled style={{background:'#eee', cursor: 'not-allowed'}}/>
                            </div>
                            {/* ПОЛЕ "РОЛЬ" ПРИБРАНО ЗВІДСИ */}
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
        }

        return null;
    };

    if (!activeTab) return <div className={styles.pageContainer}>Завантаження...</div>;

    return (
        <div className={styles.pageContainer}>
            <h1 className={styles.mainTitle}>Особистий кабінет</h1>
            <div className={styles.dashboardLayout}>
                <aside className={styles.sidebar}>
                    <ul className={styles.menuList}>
                        {menuItems.map(item => (
                            <li key={item.id} className={`${styles.menuItem} ${activeTab === item.id ? styles.active : ''}`} onClick={() => setActiveTab(item.id)}>
                                <span className={styles.menuIcon}>{item.icon}</span> {item.label}
                            </li>
                        ))}
                        <li className={styles.menuItem} onClick={handleLogout} style={{marginTop: '20px', color: '#dc3545'}}>
                            <span className={styles.menuIcon}>{Icons.logout}</span> Вийти
                        </li>
                    </ul>
                </aside>
                <main className={styles.contentArea}>
                    {renderTabContent()}
                </main>
            </div>
        </div>
    );
};

export default UserProfilePage;
