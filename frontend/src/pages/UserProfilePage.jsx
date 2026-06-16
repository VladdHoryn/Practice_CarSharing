import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './UserProfilePage.module.css';
import { authService } from '../services/auth.service';
import { userService } from '../services/user.service'; // Оновлений сервіс
import { bookingService } from '../services/booking.service';
import { carService } from '../services/car.service';
import { toast } from 'react-toastify';

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

    const [user, setUser] = useState({ id: '', firstName: '', lastName: '', email: '', role: '' });
    const [activeTab, setActiveTab] = useState('');
    const [bookings, setBookings] = useState([]);
    const [ownerCars, setOwnerCars] = useState([]);

    const [showCarModal, setShowCarModal] = useState(false);
    const [editingCar, setEditingCar] = useState(null);
    const [carForm, setCarForm] = useState({ brand: '', model: '', year: 2026, carClass: 'ECONOMY', pricePerDay: '', imageUrl: '' });


    const [profileForm, setProfileForm] = useState({ firstName: '', lastName: '' });

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);

            userService.getUserByKeycloakId(parsedUser.id)
                .then(realUserData => {
                    setUser({
                        id: parsedUser.id, // String UUID
                        firstName: realUserData.firstName || '',
                        lastName: realUserData.lastName || '',
                        email: realUserData.email || parsedUser.email,
                        role: parsedUser.role
                    });
                    setProfileForm({
                        firstName: realUserData.firstName || '',
                        lastName: realUserData.lastName || ''
                    });
                })
                .catch(() => {
                    const nameParts = parsedUser.fullName ? parsedUser.fullName.split(' ') : [''];
                    setUser({
                        id: parsedUser.id,
                        firstName: nameParts[0] || '',
                        lastName: nameParts.slice(1).join(' ') || '',
                        email: parsedUser.email,
                        role: parsedUser.role
                    });
                    setProfileForm({
                        firstName: nameParts[0] || '',
                        lastName: nameParts.slice(1).join(' ') || ''
                    });
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

    useEffect(() => {
            if (activeTab === 'fleet' && user.role === 'OWNER') {
                const storedUser = localStorage.getItem('user');
                if (!storedUser) return;
                const parsedUser = JSON.parse(storedUser);

                carService.getUnconfirmedCars()
                    .then(data => {

                        const myCars = data.filter(car => Number(car.userId) === Number(parsedUser.dbId));
                        setOwnerCars(myCars);
                    })
                    .catch(err => console.error("Помилка завантаження авто власника:", err));
            }
        }, [activeTab, user.role, user.id]);

    useEffect(() => {
        if (activeTab === 'history' && user.id) {
            const loadBookingsWithCars = async () => {
                try {
                    const bookingsData = await bookingService.getUserBookings(user.id);
                    const enrichedBookings = await Promise.all(bookingsData.map(async (booking) => {
                        try {
                            const carInfo = await carService.getCarById(booking.carId);
                            return { ...booking, carName: `${carInfo.brand} ${carInfo.model}` };
                        } catch (err) {
                            return { ...booking, carName: `Авто (Видалено з БД)` };
                        }
                    }));
                    setBookings(enrichedBookings.sort((a,b) => b.id - a.id));
                } catch (err) {
                    console.error("Помилка завантаження бронювань:", err);
                }
            };
            loadBookingsWithCars();
        }
    }, [activeTab, user.id]);

    const handleLogout = () => {
        authService.logout();
        navigate('/login');
    };

    const openCreateCarModal = () => {
        setEditingCar(null);
        setCarForm({ brand: '', model: '', year: 2026, carClass: 'ECONOMY', pricePerDay: '', imageUrl: '' });
        setShowCarModal(true);
    };

    const openEditCarModal = (car) => {
        setEditingCar(car);
        setCarForm({ brand: car.brand, model: car.model, year: car.year, carClass: car.carClass, pricePerDay: car.pricePerDay, imageUrl: car.imageUrl || '' });
        setShowCarModal(true);
    };

    const handleCarFormChange = (e) => {
        const { name, value } = e.target;
        setCarForm(prev => ({ ...prev, [name]: value }));
    };

    const submitCarForm = async (e) => {
            e.preventDefault();
            try {
                // Витягуємо збереженого при логіні користувача, щоб взяти його числовий dbId
                const storedUser = localStorage.getItem('user');
                if (!storedUser) return navigate('/login');
                const parsedUser = JSON.parse(storedUser);

                if (!parsedUser.dbId) {
                    toast.error('Помилка: у вашому профілі відсутній числовий ID бази даних. Перезайдіть у систему.');
                    return;
                }

                const payload = {
                    brand: carForm.brand,
                    model: carForm.model,
                    year: parseInt(carForm.year),
                    pricePerDay: parseFloat(carForm.pricePerDay),
                    carClass: carForm.carClass.toUpperCase(), // Завжди великими літерами для Enum
                    imageUrl: carForm.imageUrl || null,

                    // 🔥 ВИПРАВЛЕНО: Замість UUID (user.id) передаємо числовий Long ID
                    userId: Number(parsedUser.dbId)
                };

                console.log("Відправляємо дані автомобіля на бекенд:", payload);

                if (editingCar) {
                    const updatedCar = await carService.updateCar(editingCar.id, payload);
                    setOwnerCars(ownerCars.map(c => c.id === editingCar.id ? updatedCar : c));
                    toast.success('Авто успішно оновлено!');
                } else {
                    const newCar = await carService.createCar(payload);
                    setOwnerCars([...ownerCars, newCar]);
                    toast.success('Нове авто додано!');
                }
                setShowCarModal(false);
            } catch (err) {
                console.error("Помилка збереження авто:", err);
                // Витягуємо точний опис помилки валідації від Spring Boot
                const errorMsg = err.response?.data?.message || 'Помилка при збереженні авто.';
                toast.error(errorMsg);
            }
        };

    const deleteCar = async (id) => {
        if (window.confirm('Ви впевнені, що хочете видалити це авто?')) {
            try {
                await carService.deleteCar(id);
                setOwnerCars(ownerCars.filter(c => c.id !== id));
                toast.success('Авто видалено!');
            } catch (err) {
                toast.error('Помилка видалення авто.');
            }
        }
    };

    const cancelBooking = async (id) => {
        if (window.confirm('Скасувати це бронювання?')) {
            try {
                await bookingService.cancelBooking(id);
                setBookings(bookings.map(b => b.id === id ? { ...b, status: 'CANCELLED' } : b));
                toast.success('Бронювання успішно скасовано.');
            } catch (err) {
                toast.error(err.response?.data?.message || 'Не вдалося скасувати бронювання.');
            }
        }
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        try {
            const updatedData = {
                firstName: profileForm.firstName,
                lastName: profileForm.lastName,
                email: user.email
            };
            // 🚀 ВИКЛИК PUT ЗАПИТУ НА REAl BEKEND ЗА KEYCLOAK ID
            await userService.updateUserByKeycloak(user.id, updatedData);

            setUser(prev => ({ ...prev, ...updatedData }));
            toast.success('Персональні дані успішно оновлено в БД!');
        } catch (err) {
            toast.error(err.response?.data?.message || 'Не вдалося оновити дані.');
        }
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

    const mockAnalyticsDays = [
        { day: 'Пн', value: 70 }, { day: 'Вв', value: 75 }, { day: 'Ср', value: 60 },
        { day: 'Чт', value: 45 }, { day: 'Пт', value: 75 }, { day: 'Сб', value: 95 }, { day: 'Нд', value: 80 }
    ];

    const renderTabContent = () => {
        if (activeTab === 'fleet') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Керування автопарком (Список)</h2>
                    <button className={styles.addCarBtn} onClick={openCreateCarModal}>+ ДОДАТИ АВТО</button>

                    <div className={styles.filtersRow}>
                        <div className={styles.inputGroup}><label>Марка</label><select><option>all</option></select></div>
                        <div className={styles.inputGroup}><label>Статус</label><select><option>all</option></select></div>
                        <div className={styles.inputGroup}><label>Рік</label><select><option>all</option></select></div>
                        <div className={styles.inputGroup}><label>VIN</label><input type="text" placeholder="Пошук за VIN" /></div>
                    </div>

                    <table className={styles.historyTable}>
                        <thead>
                        <tr><th>Фото</th><th>Модель</th><th>Рік</th><th>Клас</th><th>Статус</th><th>Ціна</th><th>Дії</th></tr>
                        </thead>
                        <tbody>
                        {ownerCars.length > 0 ? (
                            ownerCars.map(car => (
                                <tr key={car.id}>
                                    <td>
                                        {car.imageUrl ? (
                                            <img src={car.imageUrl} alt="car" className={styles.carThumb} style={{width: '60px', height: '40px', objectFit: 'cover', borderRadius: '4px'}}/>
                                        ) : (
                                            <div style={{width: '60px', height: '40px', backgroundColor: '#eee', borderRadius: '4px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px'}}>Фото</div>
                                        )}
                                    </td>
                                    <td><strong>{car.brand} {car.model}</strong></td>
                                    <td>{car.year}</td>
                                    <td>{car.carClass}</td>
                                    <td><span style={{color: '#f39c12', fontWeight: 'bold'}}>{car.status || 'UNCONFIRMED'}</span></td>
                                    <td>{car.pricePerDay}€</td>
                                    <td style={{ verticalAlign: 'middle' }}>
                                        <div className={styles.actionsWrapper}>
                                            <span style={{color: '#3ba4f6', cursor:'pointer'}} onClick={() => openEditCarModal(car)}>Редаг.</span>
                                            <span style={{color: '#dc3545', cursor:'pointer'}} onClick={() => deleteCar(car.id)}>Видалити</span>
                                        </div>
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="7" className={styles.emptyState} style={{textAlign: 'center', padding: '20px'}}>У вас немає непідтверджених авто.</td>
                            </tr>
                        )}
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
                        <div className={styles.statCard}><div className={styles.statCardTitle}>Кількість бронювань</div><div className={styles.statCardValue}>150</div></div>
                        <div className={styles.statCard}><div className={styles.statCardTitle}>Загальна виручка</div><div className={styles.statCardValue}>250 000 грн</div></div>
                        <div className={styles.statCard}><div className={styles.statCardTitle}>Рівень завантаженості</div><div className={styles.statCardValue}>75%</div></div>
                        <div className={styles.statCard}><div className={styles.statCardTitle}>Кількість авто</div><div className={styles.statCardValue}>5</div></div>
                    </div>

                    <div className={styles.chartsGrid}>
                        <div className={styles.chartBox}>
                            <div className={styles.chartTitle}>Виручка за місяць</div>
                            <div className={styles.cssBarChart}>
                                {mockAnalyticsDays.map((item, i) => (
                                    <div key={i} className={styles.barCol}><div className={styles.barFill} style={{ height: `${item.value - 20}%` }}></div><span className={styles.barLabel}>Міс {i+1}</span></div>
                                ))}
                            </div>
                        </div>
                        <div className={styles.chartBox}>
                            <div className={styles.chartTitle}>Завантаженість по днях тижня</div>
                            <div className={styles.cssBarChart}>
                                {mockAnalyticsDays.map((item, i) => (
                                    <div key={i} className={styles.barCol}><div className={styles.barFill} style={{ height: `${item.value}%` }}></div><span className={styles.barLabel}>{item.day}</span></div>
                                ))}
                            </div>
                        </div>
                    </div>
                </>
            );
        }

        if (activeTab === 'order') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Замовити авто</h2>
                    <p className={styles.greeting}>Привіт, {user.firstName || 'Гість'}!</p>
                    <p className={styles.infoText}>Спеціально для вас ми відображаємо статус доступності для всіх авто, щоб процес підбору став для вас ще швидше та зрозуміліше.</p>
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
                        <thead>
                        <tr><th>ID Бронювання</th><th>Авто</th><th>Період</th><th>Сума</th><th>Статус</th><th>Дії</th></tr>
                        </thead>
                        <tbody>
                        {bookings.length > 0 ? (
                            bookings.map(order => (
                                <tr key={order.id}>
                                    <td>#{order.id}</td>
                                    <td><strong>{order.carName}</strong></td>
                                    <td>{order.startDate.split('T')[0]} — {order.endDate.split('T')[0]}</td>
                                    <td><strong>{order.totalPrice}€</strong></td>
                                    <td>
                                            <span className={styles.statusBadge} style={{color: order.status === 'CREATED' ? '#28a745' : (order.status === 'CANCELLED' ? '#dc3545' : '#666')}}>
                                                {order.status}
                                            </span>
                                    </td>
                                    <td>
                                        {(order.status === 'CREATED' || order.status === 'PENDING') && (
                                            <span style={{color: '#dc3545', cursor:'pointer', fontWeight: 'bold'}} onClick={() => cancelBooking(order.id)}>
                                                    Скасувати
                                                </span>
                                        )}
                                    </td>
                                </tr>
                            ))
                        ) : (
                            <tr><td colSpan="6" className={styles.emptyState}>У вас ще немає замовлень.</td></tr>
                        )}
                        </tbody>
                    </table>
                </>
            );
        }

        if (activeTab === 'docs') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Завантажені документи</h2>
                    <p className={styles.infoText}>Тут ви можете завантажити зображення документів, що засвідчують вашу особу.</p>
                    <div className={styles.docRow}><div className={styles.docLabel}>Паспорт 1 та 2 сторінка</div><input type="file" className={styles.docInput} /></div>
                    <div className={styles.docRow}><div className={styles.docLabel}>Паспорт прописка</div><input type="file" className={styles.docInput} /></div>
                    <div className={styles.docRow}><div className={styles.docLabel}>Водійське посвідчення</div><input type="file" className={styles.docInput} /></div>
                    <button className={styles.primaryBtn} style={{marginTop: '20px'}}>Надіслати</button>
                </>
            );
        }

        if (activeTab === 'profile') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Персональні дані</h2>
                    <form onSubmit={handleProfileSubmit}>
                        <div className={styles.formGrid}>
                            <div className={styles.inputGroup}>
                                <label>Ім'я</label>
                                <input type="text" value={profileForm.firstName} onChange={(e) => setProfileForm({...profileForm, firstName: e.target.value})} />
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Прізвище</label>
                                <input type="text" value={profileForm.lastName} onChange={(e) => setProfileForm({...profileForm, lastName: e.target.value})} />
                            </div>
                            <div className={styles.inputGroup}><label>Email (не змінюється)</label><input type="email" value={user.email} disabled style={{background:'#eee', cursor: 'not-allowed'}}/></div>
                        </div>
                        <h3 className={styles.sectionSubtitle}>Зміна пароля (через Keycloak Console)</h3>
                        <div className={styles.formGrid}>
                            <div className={styles.inputGroup}><label>Новий пароль</label><input type="password" disabled placeholder="Зміна в кабінеті Keycloak" style={{background:'#eee'}}/></div>
                            <div className={styles.inputGroup}><label>Повторіть новий пароль</label><input type="password" disabled placeholder="Зміна в кабінеті Keycloak" style={{background:'#eee'}}/></div>
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

            {showCarModal && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                    <div style={{ background: '#fff', padding: '30px', borderRadius: '12px', width: '500px', maxWidth: '90%' }}>
                        <h2 style={{marginTop: 0}}>{editingCar ? 'Редагувати авто' : 'Додати нове авто'}</h2>
                        <form onSubmit={submitCarForm}>
                            <div style={{display: 'flex', gap: '10px', marginBottom: '10px'}}>
                                <div style={{flex: 1}}><label>Марка</label><input required type="text" name="brand" value={carForm.brand} onChange={handleCarFormChange} style={{width:'100%', padding:'8px'}}/></div>
                                <div style={{flex: 1}}><label>Модель</label><input required type="text" name="model" value={carForm.model} onChange={handleCarFormChange} style={{width:'100%', padding:'8px'}}/></div>
                            </div>
                            <div style={{display: 'flex', gap: '10px', marginBottom: '10px'}}>
                                <div style={{flex: 1}}><label>Рік випуску</label><input required type="number" name="year" value={carForm.year} onChange={handleCarFormChange} style={{width:'100%', padding:'8px'}}/></div>
                                <div style={{flex: 1}}><label>Ціна (€/доба)</label><input required type="number" step="0.1" name="pricePerDay" value={carForm.pricePerDay} onChange={handleCarFormChange} style={{width:'100%', padding:'8px'}}/></div>
                            </div>
                            <div style={{marginBottom: '10px'}}>
                                <label>Клас авто</label>
                                <select name="carClass" value={carForm.carClass} onChange={handleCarFormChange} style={{width:'100%', padding:'8px'}}>
                                    <option value="ECONOMY">Economy</option>
                                    <option value="COMFORT">Comfort</option>
                                    <option value="BUSINESS">Business</option>
                                    <option value="LUXURY">Luxury</option>
                                </select>
                            </div>
                            <div style={{marginBottom: '20px'}}>
                                <label>URL фотографії</label>
                                <input type="url" name="imageUrl" value={carForm.imageUrl} onChange={handleCarFormChange} placeholder="https://..." style={{width:'100%', padding:'8px'}}/>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <button type="button" onClick={() => setShowCarModal(false)} style={{ padding: '10px 20px', background: '#ccc', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Скасувати</button>
                                <button type="submit" style={{ padding: '10px 20px', background: '#0056b3', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Зберегти</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserProfilePage;
