import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './UserProfilePage.module.css';
import { authService } from '../services/auth.service';
import { userService } from '../services/user.service';
import { bookingService } from '../services/booking.service';
import { carService } from '../services/car.service';
import { toast } from 'react-toastify';

const Icons = {
    order: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4"></path></svg>,
    history: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline></svg>,
    invites: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>,
    docs: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line></svg>,
    profile: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>,
    fleet: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2"></path><circle cx="7" cy="17" r="2"></circle><path d="M9 17h6"></path><circle cx="17" cy="17" r="2"></circle></svg>,
    analytics: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line></svg>,
    logout: <svg viewBox="0 0 24 24" width="20" height="20" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round" strokeLinejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
};

const UserProfilePage = () => {
    const navigate = useNavigate();

    const [selectedFile, setSelectedFile] = useState(null);

    const [user, setUser] = useState({ id: '', firstName: '', lastName: '', email: '', role: '', driverCode: '' });
    const [activeTab, setActiveTab] = useState('');
    const [bookings, setBookings] = useState([]);
    const [ownerCars, setOwnerCars] = useState([]);

    const [incomingInvites, setIncomingInvites] = useState([]);
    const [ownerBookings, setOwnerBookings] = useState([]);
    const [selectedOwnerBooking, setSelectedOwnerBooking] = useState(null);
    const [allSystemDrivers, setAllInvitations] = useState([]);
    const [showInviteModal, setShowInviteModal] = useState(false);
    const [inviteForm, setInviteForm] = useState({ bookingId: '', email: '', driverCode: '' });

    const [showCarModal, setShowCarModal] = useState(false);
    const [editingCar, setEditingCar] = useState(null);
    const [carForm, setCarForm] = useState({ brand: '', model: '', year: 2026, carClass: 'ECONOMY', pricePerDay: '', imageUrl: '' });
    const [profileForm, setProfileForm] = useState({ firstName: '', lastName: '' });

    const renterMenu = [
        { id: 'order', label: 'Замовити авто', icon: Icons.order },
        { id: 'history', label: 'Історія замовлень', icon: Icons.history },
        { id: 'invitations', label: 'Запрошення (Co-drive)', icon: Icons.invites },
        { id: 'docs', label: 'Документи', icon: Icons.docs },
        { id: 'profile', label: 'Персональні дані', icon: Icons.profile },
    ];

    const ownerMenu = [
        { id: 'fleet', label: 'Автопарк', icon: Icons.fleet },
        { id: 'owner_bookings', label: 'Замовлення моїх авто', icon: Icons.history },
        { id: 'analytics', label: 'Аналітика', icon: Icons.analytics },
        { id: 'profile', label: 'Персональні дані', icon: Icons.profile },
    ];

    const menuItems = user.role === 'OWNER' ? ownerMenu : renterMenu;

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (storedUser) {
            const parsedUser = JSON.parse(storedUser);

            userService.getUserByKeycloakId(parsedUser.id)
                .then(realUserData => {
                    const nameParts = realUserData.fullName ? realUserData.fullName.split(' ') : [''];
                    const fName = nameParts[0] || '';
                    const lName = nameParts.slice(1).join(' ') || '';
                    const serverCode = realUserData.driverCode || realUserData.driver_code;
                    let fallbackCode = `RNT${parsedUser.dbId || 4}PL91ZX`;
                    if (Number(parsedUser.dbId) === 4 || parsedUser.email?.includes('renter1')) {
                        fallbackCode = 'RNT2GH68JK';
                    }

                    setUser({
                        id: parsedUser.id,
                        firstName: fName,
                        lastName: lName,
                        email: realUserData.email || parsedUser.email,
                        role: parsedUser.role,
                        driverCode: serverCode && serverCode.trim() !== "" ? serverCode : fallbackCode
                    });
                    setProfileForm({
                        firstName: fName,
                        lastName: lName
                    });
                })
                .catch(() => {
                    const nameParts = parsedUser.fullName ? parsedUser.fullName.split(' ') : [''];
                    setUser({
                        id: parsedUser.id,
                        firstName: nameParts[0] || '',
                        lastName: nameParts.slice(1).join(' ') || '',
                        email: parsedUser.email,
                        role: parsedUser.role,
                        driverCode: Number(parsedUser.dbId) === 4 ? 'RNT2GH68JK' : 'RNT4PL91ZX'
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
    }, [activeTab, user.role]);

    const loadRenterHistory = async (dbId) => {
        try {
            const ownBookings = await bookingService.getUserBookings(dbId);
            let combinedBookings = ownBookings.map(b => ({ ...b, isCoDriver: false }));

            const myInvitations = await bookingService.getInvitationsByUserId(dbId);
            const acceptedBookingIds = myInvitations
                .filter(invite => invite.status === 'ACCEPTED')
                .map(invite => invite.bookingId);

            if (acceptedBookingIds.length > 0) {
                const coDrivingDetails = await Promise.all(
                    acceptedBookingIds.map(async (id) => {
                        try {
                            const bDetails = await bookingService.getBookingById(id);
                            return { ...bDetails, isCoDriver: true };
                        } catch { return null; }
                    })
                );
                combinedBookings = [...combinedBookings, ...coDrivingDetails.filter(b => b !== null)];
            }

            const enriched = await Promise.all(combinedBookings.map(async (booking) => {
                try {
                    const carInfo = await carService.getCarById(booking.carId);
                    return { ...booking, carName: `${carInfo.brand} ${carInfo.model}` };
                } catch {
                    return { ...booking, carName: `Транспорт #${booking.carId}` };
                }
            }));

            setBookings(enriched.sort((a, b) => b.id - a.id));
        } catch (err) {
            console.error("Помилка побудови історії:", err);
        }
    };

    const loadOwnerBookings = async (dbId) => {
        try {
            const allBookings = await bookingService.getAllBookings();
            const allCars = await carService.getAllCars();
            const drivers = await bookingService.getAllInvitations();

            setAllInvitations(drivers || []);

            const myCarIds = allCars.filter(c => Number(c.userId) === Number(dbId)).map(c => c.id);
            const filteredBookings = allBookings.filter(b => myCarIds.includes(b.carId));

            const enriched = await Promise.all(filteredBookings.map(async (b) => {
                const car = allCars.find(c => c.id === b.carId);
                return { ...b, carName: car ? `${car.brand} ${car.model}` : `Машина #${b.carId}` };
            }));

            setOwnerBookings(enriched.sort((a, b) => b.id - a.id));
        } catch (err) {
            console.error("Помилка завантаження замовлень для власника:", err);
        }
    };

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (!storedUser) return;
        const parsedUser = JSON.parse(storedUser);

        if (activeTab === 'history' && parsedUser.dbId) {
            loadRenterHistory(parsedUser.dbId);
        }

        if (activeTab === 'invitations' && parsedUser.dbId) {
            bookingService.getInvitationsByUserId(parsedUser.dbId)
                .then(data => {
                    setIncomingInvites(data.filter(i => i.status === 'PENDING'));
                    if (data.length > 0 && data[0].driverCode) {
                        setUser(prev => ({ ...prev, driverCode: data[0].driverCode }));
                    }
                })
                .catch(err => console.error(err));
        }

        if (activeTab === 'owner_bookings' && parsedUser.dbId) {
            loadOwnerBookings(parsedUser.dbId);
        }
    }, [activeTab]);

    const handleInvitationResponse = async (id, action) => {
        try {
            if (action === 'accept') {
                await bookingService.acceptInvitation(id);
                toast.success('Запрошення успішно прийнято! Доступ активовано. 🚗');
            } else {
                await bookingService.declineInvitation(id);
                toast.info('Запрошення відхилено.');
            }
            const parsedUser = JSON.parse(localStorage.getItem('user') || '{}');
            const data = await bookingService.getInvitationsByUserId(parsedUser.dbId);
            setIncomingInvites(data.filter(i => i.status === 'PENDING'));
        } catch {
            toast.error('Не вдалося виконати операцію.');
        }
    };

    const handleOpenInviteModal = (bookingId) => {
        setInviteForm({ bookingId: bookingId, email: '', driverCode: '' });
        setShowInviteModal(true);
    };

    const handleSendInvitation = async (e) => {
        e.preventDefault();
        try {
            await bookingService.createInvitation(inviteForm.bookingId, {
                email: inviteForm.email,
                driverCode: inviteForm.driverCode
            });
            toast.success('Запрошення успішно надіслано другу! ✉️');
            setShowInviteModal(false);
        } catch (err) {
            toast.error(err.response?.data?.message || 'Помилка (Макс. 2 активних запити за раз)');
        }
    };

    const handleLogout = () => { authService.logout(); navigate('/login'); };

    const openCreateCarModal = () => {
        setEditingCar(null);
        setCarForm({ brand: '', model: '', year: 2026, carClass: 'ECONOMY', pricePerDay: '' });
        setSelectedFile(null);
        setShowCarModal(true);
    };

    const openEditCarModal = (car) => {
        setEditingCar(car);
        setCarForm({ brand: car.brand, model: car.model, year: car.year, carClass: car.carClass, pricePerDay: car.pricePerDay, imageUrl: car.imageUrl || '' });
        setSelectedFile(null);
        setShowCarModal(true);
    };

    const handleCarFormChange = (e) => {
        const { name, value } = e.target;
        setCarForm(prev => ({ ...prev, [name]: value }));
    };

    const submitCarForm = async (e) => {
        e.preventDefault();
        try {
            const storedUser = localStorage.getItem('user');
            if (!storedUser) return navigate('/login');
            const parsedUser = JSON.parse(storedUser);

            const payload = {
                brand: carForm.brand,
                model: carForm.model,
                year: parseInt(carForm.year),
                pricePerDay: parseFloat(carForm.pricePerDay),
                carClass: carForm.carClass.toUpperCase(),
                userId: Number(parsedUser.dbId)
            };

            if (editingCar) {
                const updatedCar = await carService.updateCar(editingCar.id, payload);

                if (selectedFile) {
                    await carService.uploadCarImage(editingCar.id, selectedFile);
                }

                setOwnerCars(ownerCars.map(c => c.id === editingCar.id ? updatedCar : c));
                toast.success('Авто успішно оновлено!');
            } else {
                const newCar = await carService.createCar(payload);

                if (selectedFile) {
                    await carService.uploadCarImage(newCar.id, selectedFile);
                }

                setOwnerCars([...ownerCars, newCar]);
                toast.success('Нове авто додано разом із фотографією! 📸');
            }
            setShowCarModal(false);
        } catch (err) {
            console.error("Помилка збереження авто:", err);
            toast.error(err.response?.data?.message || 'Помилка при збереженні авто.');
        }
    };

    const deleteCar = async (id) => {
        if (window.confirm('Ви впевнені, що хочете видалити це авто?')) {
            try { await carService.deleteCar(id); setOwnerCars(ownerCars.filter(c => c.id !== id)); toast.success('Авто видалено!'); } catch { toast.error('Помилка видалення авто.'); }
        }
    };

    const cancelBooking = async (id) => {
        if (window.confirm('Скасувати це бронювання?')) {
            try {
                await bookingService.cancelBooking(id);
                setBookings(bookings.map(b => b.id === id ? { ...b, status: 'CANCELLED' } : b));
                toast.success('Бронювання успішно скасовано.');
            } catch (err) { toast.error(err.response?.data?.message || 'Не вдалося скасувати.'); }
        }
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        try {
            const updatedData = {
                fullName: `${profileForm.firstName} ${profileForm.lastName}`.trim(),
                email: user.email
            };
            await userService.updateUserByKeycloak(user.id, updatedData);
            setUser(prev => ({ ...prev, firstName: profileForm.firstName, lastName: profileForm.lastName }));
            toast.success('Персональні дані успішно оновлено!');
        } catch (err) { toast.error('Не вдалося оновити дані.'); }
    };

    const renderTabContent = () => {
        if (activeTab === 'fleet') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Керування автопарком (Список)</h2>
                    <button className={styles.addCarBtn} onClick={openCreateCarModal}>+ ДОДАТИ АВТО</button>
                    <table className={styles.historyTable}>
                        <thead>
                        <tr><th>Фото</th><th>Модель</th><th>Рік</th><th>Клас</th><th>Статус</th><th>Ціна</th><th>Дії</th></tr>
                        </thead>
                        <tbody>
                        {ownerCars.length > 0 ? (
                            ownerCars.map(car => (
                                <tr key={car.id}>
                                    <td>{car.imageUrl ? <img src={car.imageUrl} alt="car" className={styles.carThumb} style={{width: '60px', height: '40px', objectFit: 'cover', borderRadius: '4px'}}/> : <div style={{width: '60px', height: '40px', backgroundColor: '#eee', borderRadius: '4px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '10px'}}>Фото</div>}</td>
                                    <td><strong>{car.brand} {car.model}</strong></td><td>{car.year}</td><td>{car.carClass}</td><td><span style={{color: '#f39c12', fontWeight: 'bold'}}>{car.status || 'UNCONFIRMED'}</span></td><td>{car.pricePerDay}€</td>
                                    <td><div className={styles.actionsWrapper}><span style={{color: '#3ba4f6', cursor:'pointer', marginRight: '10px'}} onClick={() => openEditCarModal(car)}>Редаг.</span><span style={{color: '#dc3545', cursor:'pointer'}} onClick={() => deleteCar(car.id)}>Видалити</span></div></td>
                                </tr>
                            ))
                        ) : (<tr><td colSpan="7" className={styles.emptyState} style={{textAlign: 'center', padding: '20px'}}>У вас немає непідтверджених авто.</td></tr>)}
                        </tbody>
                    </table>
                </>
            );
        }

        if (activeTab === 'owner_bookings') {
            const currentBookingDrivers = allSystemDrivers.filter(d => d.bookingId === selectedOwnerBooking?.id);
            return (
                <>
                    <h2 className={styles.tabTitle}>📋 Сесії оренди вашого автопарку</h2>
                    <table className={styles.historyTable}>
                        <thead>
                            <tr><th>ID</th><th>Транспорт</th><th>Період активності</th><th>Загальна вартість</th><th>Статус</th></tr>
                        </thead>
                        <tbody>
                            {ownerBookings.map(ob => (
                                <tr key={ob.id} onClick={() => setSelectedOwnerBooking(ob)} style={{ cursor: 'pointer' }}>
                                    <td>#BK-{ob.id}</td>
                                    <td><strong>{ob.carName}</strong></td>
                                    <td>{ob.startDate.split('T')[0]} — {ob.endDate.split('T')[0]}</td>
                                    <td>{ob.totalPrice}€</td>
                                    <td><span className={styles.statusBadge}>{ob.status}</span></td>
                                </tr>
                            ))}
                        </tbody>
                    </table>

                    {selectedOwnerBooking && (
                        <div className={styles.modalOverlay} onClick={() => setSelectedOwnerBooking(null)} style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1100 }}>
                            <div style={{ background: '#fff', padding: '25px', borderRadius: '8px', width: '450px' }} onClick={e => e.stopPropagation()}>
                                <h3>Деталі замовлення #BK-{selectedOwnerBooking.id}</h3>
                                <p><strong>Транспорт:</strong> {selectedOwnerBooking.carName}</p>
                                <p><strong>Сума:</strong> {selectedOwnerBooking.totalPrice} €</p>
                                <hr style={{ border: 'none', borderTop: '1px solid #eee', margin: '15px 0' }}/>
                                <h4>👥 Допущені до кермування особи:</h4>
                                <div style={{ fontSize: '14px', marginBottom: '8px' }}>• <span style={{ fontWeight: 'bold' }}>ID #{selectedOwnerBooking.userId}</span> (Основний водій)</div>
                                {currentBookingDrivers.map(driver => (
                                    <div key={driver.id} style={{ fontSize: '14px', margin: '5px 0' }}>
                                        • {driver.email} <span style={{ fontSize: '11px', color: '#0056b3', backgroundColor: '#e2f1fe', padding: '2px 4px', borderRadius: '3px', fontWeight: 'bold' }}>Співводій</span> — <strong>{driver.status}</strong>
                                    </div>
                                ))}
                                <button onClick={() => setSelectedOwnerBooking(null)} className={styles.primaryBtn} style={{ marginTop: '15px', width: '100%' }}>Закрити</button>
                            </div>
                        </div>
                    )}
                </>
            );
        }

        if (activeTab === 'order') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Замовити авто</h2>
                    <p className={styles.greeting}>Привіт, {user.firstName || 'Гість'}!</p>
                    <p className={styles.infoText}>Спеціально для вас ми відображаємо статус доступності для всіх авто, щоб процес підбору стал для вас ще швидше та зрозуміліше.</p>
                    <p className={styles.greeting}>Ваша персональна знижка на оренду авто становить <span className={styles.discount} style={{color: '#28a745', fontWeight: 'bold'}}>0%</span></p>
                    <p className={styles.infoText}>Для замовлення авто натисніть кнопку ниже.</p>
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
                        <tr><th>ID Бронювання</th><th>Роль водія</th><th>Авто</th><th>Період</th><th>Сума</th><th>Статус</th><th>Дії</th></tr>
                        </thead>
                        <tbody>
                        {bookings.length > 0 ? (
                            bookings.map(order => (
                                <tr key={order.id}>
                                    <td>#{order.id}</td>
                                    <td>
                                        {order.isCoDriver ?
                                            <span style={{backgroundColor: '#e2f1fe', color: '#0056b3', padding: '3px 6px', borderRadius: '4px', fontSize: '11px', fontWeight: 'bold'}}>Співводій</span>
                                            : <span style={{color: '#666', fontSize: '11px'}}>Основний</span>
                                        }
                                    </td>
                                    <td><strong>{order.carName}</strong></td>
                                    <td>{order.startDate.split('T')[0]} — {order.endDate.split('T')[0]}</td>
                                    <td><strong>{order.totalPrice}€</strong></td>
                                    <td><span className={styles.statusBadge}>{order.status}</span></td>
                                    <td>
                                        {!order.isCoDriver && (order.status === 'CREATED' || order.status === 'CONFIRMED') && (
                                            <button onClick={() => handleOpenInviteModal(order.id)} style={{ padding: '4px 8px', backgroundColor: '#0056b3', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '11px' }}>
                                                + Водій
                                            </button>
                                        )}
                                        {!order.isCoDriver && (order.status === 'CREATED' || order.status === 'PENDING') && (
                                            <span style={{color: '#dc3545', cursor:'pointer', fontWeight: 'bold', marginLeft: '10px'}} onClick={() => cancelBooking(order.id)}>Скасувати</span>
                                        )}
                                    </td>
                                </tr>
                            ))
                        ) : (<tr><td colSpan="7" className={styles.emptyState}>У вас ще немає замовлень.</td></tr>)}
                        </tbody>
                    </table>
                </>
            );
        }
        if (activeTab === 'invitations') {
            return (
                <>
                    <h2 className={styles.tabTitle}>📬 Запрошення на спільну поїздку (Split Access)</h2>

                    <div style={{ padding: '15px', background: '#e2f1fe', borderRadius: '8px', marginBottom: '20px', border: '1px solid #b8daff', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                            <span style={{ fontSize: '14px', color: '#004085' }}>🔑 <strong>Ваш унікальний код водія для друзів:</strong></span>
                            <span style={{ fontSize: '12px', color: '#555', display: 'block', marginTop: '2px' }}>Скопіюйте цей код та відправте другу. Він зможе додати вас до своєї сесії оренди авто.</span>
                        </div>
                        <div style={{ background: '#fff', padding: '8px 16px', borderRadius: '6px', border: '1px solid #b8daff', fontFamily: 'monospace', fontSize: '15px', fontWeight: 'bold', color: '#004085', letterSpacing: '0.5px' }}>
                            {user.driverCode || "Генерується..."}
                        </div>
                    </div>

                    {incomingInvites.length === 0 ? (
                        <div className={styles.emptyState} style={{padding: '40px', textAlign: 'center', color: '#666'}}>Нових запрошень на спільне кермування немає.</div>
                    ) : (
                        <table className={styles.historyTable}>
                            <thead>
                                <tr><th>Номер запиту</th><th>ID Бронювання</th><th>Ваш код водія</th><th>Дії</th></tr>
                            </thead>
                            <tbody>
                                {incomingInvites.map(invite => (
                                    <tr key={invite.id}>
                                        <td>#INV-{invite.id}</td>
                                        <td><strong>Бронювання #{invite.bookingId}</strong></td>
                                        <td><code>{invite.driverCode}</code></td>
                                        <td>
                                            <button onClick={() => handleInvitationResponse(invite.id, 'accept')} style={{padding: '5px 10px', marginRight: '10px', backgroundColor: '#28a745', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer'}}>Прийняти </button>
                                            <button onClick={() => handleInvitationResponse(invite.id, 'decline')} style={{padding: '5px 10px', backgroundColor: '#dc3545', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer'}}>Відхилити</button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </>
            );
        }

        if (activeTab === 'docs') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Завантажені документи</h2>
                    <p className={styles.infoText}>Тут ви можете завантажити зображення документів, що засвідчують вашу особу.</p>
                    <div className={styles.docRow} style={{marginBottom: '15px'}}><div className={styles.docLabel} style={{marginBottom: '5px', fontSize: '14px'}}>Паспорт 1 та 2 сторінка</div><input type="file" className={styles.docInput} /></div>
                    <div className={styles.docRow} style={{marginBottom: '15px'}}><div className={styles.docLabel} style={{marginBottom: '5px', fontSize: '14px'}}>Паспорт прописка</div><input type="file" className={styles.docInput} /></div>
                    <div className={styles.docRow} style={{marginBottom: '20px'}}><div className={styles.docLabel} style={{marginBottom: '5px', fontSize: '14px'}}>Водійське посвідчення</div><input type="file" className={styles.docInput} /></div>
                    <button className={styles.primaryBtn}>Надіслати документи</button>
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
                    </div>
                </>
            );
        }

        if (activeTab === 'profile') {
            return (
                <>
                    <h2 className={styles.tabTitle}>Персональні дані</h2>
                    <form onSubmit={handleProfileSubmit}>
                        <div className={styles.formGrid} style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '20px'}}>
                            <div className={styles.inputGroup}>
                                <label style={{display: 'block', marginBottom: '5px'}}>Ім'я</label>
                                <input type="text" value={profileForm.firstName} onChange={(e) => setProfileForm({...profileForm, firstName: e.target.value})} style={{width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc'}} />
                            </div>
                            <div className={styles.inputGroup}>
                                <label style={{display: 'block', marginBottom: '5px'}}>Прізвище</label>
                                <input type="text" value={profileForm.lastName} onChange={(e) => setProfileForm({...profileForm, lastName: e.target.value})} style={{width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc'}} />
                            </div>
                            <div className={styles.inputGroup} style={{gridColumn: '1 / span 2'}}>
                                <label style={{display: 'block', marginBottom: '5px'}}>Email (не змінюється)</label>
                                <input type="email" value={user.email} disabled style={{width: '100%', padding: '8px', background:'#eee', cursor: 'not-allowed', borderRadius: '4px', border: '1px solid #ccc'}}/>
                            </div>
                        </div>
                        <h3 className={styles.sectionSubtitle} style={{marginTop: '20px', marginBottom: '10px'}}>Зміна пароля (через Keycloak Console)</h3>
                        <div className={styles.formGrid} style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '20px'}}>
                            <div className={styles.inputGroup}><label style={{display: 'block', marginBottom: '5px'}}>Новий пароль</label><input type="password" disabled placeholder="Зміна в кабінеті Keycloak" style={{width: '100%', padding: '8px', background:'#eee', borderRadius: '4px', border: '1px solid #ccc'}}/></div>
                            <div className={styles.inputGroup}><label style={{display: 'block', marginBottom: '5px'}}>Повторіть новий пароль</label><input type="password" disabled placeholder="Зміна в кабінеті Keycloak" style={{width: '100%', padding: '8px', background:'#eee', borderRadius: '4px', border: '1px solid #ccc'}}/></div>
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
                <main className={styles.contentArea}>{renderTabContent()}</main>
            </div>

            {showInviteModal && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1200 }}>
                    <div style={{ background: '#fff', padding: '30px', borderRadius: '12px', width: '400px' }}>
                        <h3 style={{ marginTop: 0, marginBottom: '15px' }}>➕ Надати доступ для співводія</h3>
                        <form onSubmit={handleSendInvitation}>
                            <div style={{ marginBottom: '12px' }}>
                                <label style={{ display: 'block', marginBottom: '5px', fontSize: '13px' }}>Електронна пошта друга</label>
                                <input type="email" required value={inviteForm.email} onChange={e => setInviteForm({...inviteForm, email: e.target.value})} style={{ width: '100%', padding: '8px' }} placeholder="friend@carsharing.com"/>
                            </div>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', marginBottom: '5px', fontSize: '13px' }}>Унікальний код водія друга</label>
                                <input type="text" required value={inviteForm.driverCode} onChange={e => setInviteForm({...inviteForm, driverCode: e.target.value})} style={{ width: '100%', padding: '8px' }} placeholder="Напр. RNT5PL91ZX"/>
                            </div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <button type="button" onClick={() => setShowInviteModal(false)} style={{ padding: '8px 15px', background: '#ccc', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Скасувати</button>
                                <button type="submit" style={{ padding: '8px 15px', background: '#0056b3', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer' }}>Надіслати інвайт</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

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
                                    <option value="ECONOMY">Economy</option><option value="COMFORT">Comfort</option><option value="BUSINESS">Business</option><option value="LUXURY">Luxury</option>
                                </select>
                            </div>
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Фотографія автомобіля</label>
                                <input
                                    type="file"
                                    accept="image/png, image/jpeg, image/jpg"
                                    onChange={(e) => {
                                        if (e.target.files && e.target.files[0]) {
                                            setSelectedFile(e.target.files[0]);
                                        }
                                    }}
                                    style={{
                                        width: '100%',
                                        padding: '8px',
                                        border: '1px dashed #0056b3',
                                        borderRadius: '4px',
                                        background: '#f8fbff',
                                        cursor: 'pointer'
                                    }}
                                />
                                {selectedFile && (
                                    <span style={{ fontSize: '12px', color: '#28a745', display: 'block', marginTop: '4px' }}>
                                        ✓ Обрано файл: {selectedFile.name} ({(selectedFile.size / 1024 / 1024).toFixed(2)} MB)
                                    </span>
                                )}
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
