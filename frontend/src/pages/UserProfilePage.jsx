import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './UserProfilePage.module.css';
import { authService } from '../services/auth.service';
import { userService } from '../services/user.service';
import { bookingService } from '../services/booking.service';
import { carService } from '../services/car.service';
import { toast } from 'react-toastify';
import SecureImage from '../components/SecureImage';
import { analyticsService } from '../services/analytics.service';
import { documentService } from '../services/document.service';


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

    const [selectedFiles, setSelectedFiles] = useState([]);
    const [ownerBookingFilter, setOwnerBookingFilter] = useState('ALL');

    const [user, setUser] = useState({ id: '', firstName: '', lastName: '', email: '', role: '', driverCode: '' });
    const [activeTab, setActiveTab] = useState('');
    const [bookings, setBookings] = useState([]);
    const [ownerCars, setOwnerCars] = useState([]);
    const [uploadedDocs, setUploadedDocs] = useState([]);
    const [isProfileVerified, setIsProfileVerified] = useState(false);
    const [docsLoading, setDocsLoading] = useState(false);

    const [incomingInvites, setIncomingInvites] = useState([]);
    const [ownerBookings, setOwnerBookings] = useState([]);
    const [selectedOwnerBooking, setSelectedOwnerBooking] = useState(null);
    const [allSystemDrivers, setAllInvitations] = useState([]);
    const [showInviteModal, setShowInviteModal] = useState(false);
    const [inviteForm, setInviteForm] = useState({ bookingId: '', email: '', driverCode: '' });
    const [ownerAnalytics, setOwnerAnalytics] = useState(null);
    const [analyticsLoading, setAnalyticsLoading] = useState(false);

    const [showCarModal, setShowCarModal] = useState(false);
    const [editingCar, setEditingCar] = useState(null);
    const [carForm, setCarForm] = useState({ brand: '', model: '', year: 2026, carClass: 'ECONOMY', pricePerDay: '', imageUrl: '' });
    const [profileForm, setProfileForm] = useState({ firstName: '', lastName: '' });
    const [passwordForm, setPasswordForm] = useState({ newPassword: '', confirmPassword: '' });
    const [isChangingPassword, setIsChangingPassword] = useState(false);
    const [expandedBookingId, setExpandedBookingId] = useState(null);
    const [bookingCoDrivers, setBookingCoDrivers] = useState([]);
    const [coDriversLoading, setCoDriversLoading] = useState(false);
    const [previewDoc, setPreviewDoc] = useState(null);
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

                    // 👑 КРИТИЧНИЙ ФІКС БЛОКУВАННЯ: Якщо адмін заблокував юзера — моментально викидаємо з системи
                    if (realUserData && realUserData.isActive === false) {
                        toast.error("🛑 Ваш обліковий запис було заблоковано адміністратором системи.");
                        authService.logout();
                        navigate('/login');
                        return;
                    }

                    const nameParts = realUserData.fullName ? realUserData.fullName.split(' ') : [''];
                    const fName = nameParts[0] || '';
                    const lName = nameParts.slice(1).join(' ') || '';

                    setUser({
                        id: parsedUser.id,
                        firstName: fName,
                        lastName: lName,
                        email: realUserData.email || parsedUser.email,
                        role: parsedUser.role,
                        driverCode: realUserData.driverCode || realUserData.driver_code || 'Генерується...'
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
                        driverCode: parsedUser.driverCode || 'Немає зв\'язку'
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


            carService.getCarsByOwnerId(parsedUser.dbId)
                .then(data => {
                    setOwnerCars(data);
                })
                .catch(err => console.error("Помилка завантаження всього автопарку власника:", err));
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
        const uniqueBookings = enriched.filter((value, index, self) =>
            self.findIndex(b => b.id === value.id) === index
        );

        setBookings(uniqueBookings.sort((a, b) => b.id - a.id));


        } catch (err) {
            console.error("Помилка побудови історії:", err);
        }
    };

    const loadOwnerBookings = async (dbId) => {
        try {
            const ownerBookingsData = await bookingService.getBookingsByOwnerId(dbId);
            const ownerCarsData = await carService.getCarsByOwnerId(dbId);
            const drivers = await bookingService.getAllInvitations();

            setAllInvitations(drivers || []);

            const enriched = ownerBookingsData.map(b => {
                const car = ownerCarsData.find(c => c.id === b.carId);
                return { ...b, carName: car ? `${car.brand} ${car.model}` : `Машина #${b.carId}` };
            });

            setOwnerBookings(enriched);
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
                    })
                    .catch(err => console.error(err));
            }

            if (activeTab === 'owner_bookings' && parsedUser.dbId) {
                loadOwnerBookings(parsedUser.dbId);
            }

            if (activeTab === 'analytics' && parsedUser.dbId) {
                setAnalyticsLoading(true);
                analyticsService.getOwnerSummary(parsedUser.dbId)
                    .then(data => {
                        setOwnerAnalytics(data);
                    })
                    .catch(err => {
                        console.error("Помилка завантаження модуля аналітики OWNER:", err);
                    })
                    .finally(() => {
                        setAnalyticsLoading(false);
                    });
            }


            if (['order', 'invitations', 'docs'].includes(activeTab) && parsedUser.dbId) {
                documentService.getProfileStatus(parsedUser.dbId)
                    .then(status => {
                        setIsProfileVerified(status);
                    })
                    .catch(err => {
                        if (err.response?.status === 404) {
                            setIsProfileVerified(false);
                        } else {
                            console.error("Помилка перевірки KYC статусу:", err);
                        }
                    });

                documentService.getMetadata(parsedUser.dbId)
                    .then(meta => {
                        setUploadedDocs(meta || []);
                    })
                    .catch(err => {
                        if (err.response?.status === 404) {
                            setUploadedDocs([]);
                        } else {
                            console.error("Помилка завантаження метаданих документів:", err);
                        }
                    });
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
    const toggleCoDriversDropdown = async (bookingId) => {
        if (expandedBookingId === bookingId) {
            setExpandedBookingId(null);
            setBookingCoDrivers([]);
            return;
        }

        try {
            setCoDriversLoading(true);
            setExpandedBookingId(bookingId);
            const drivers = await bookingService.getActiveCoDriversByBookingId(bookingId);
            setBookingCoDrivers(drivers || []);
        } catch (err) {
            console.error("Помилка завантаження статусів співводіїв:", err);
            toast.error("Не вдалося завантажити статуси додаткових водіїв.");
        } finally {
            setCoDriversLoading(false);
        }
    };

    const handleLogout = () => { authService.logout(); navigate('/login'); };

    const openCreateCarModal = () => {
            setEditingCar(null);
            setCarForm({ brand: '', model: '', year: 2026, carClass: 'ECONOMY', pricePerDay: '' });

            setSelectedFiles([]);
            setShowCarModal(true);
        };

        const openEditCarModal = (car) => {
            setEditingCar(car);
            setCarForm({ brand: car.brand, model: car.model, year: car.year, carClass: car.carClass, pricePerDay: car.pricePerDay, imageUrl: car.imageUrl || '' });

            setSelectedFiles([]);
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

                if (selectedFiles.length > 0) {
                    for (const file of selectedFiles) {
                        await carService.uploadCarImage(editingCar.id, file);
                    }
                }

                setOwnerCars(ownerCars.map(c => c.id === editingCar.id ? updatedCar : c));
                toast.success('Авто успішно оновлено!');
            } else {
                const newCar = await carService.createCar(payload);


                if (selectedFiles.length > 0) {
                    for (const file of selectedFiles) {
                        await carService.uploadCarImage(newCar.id, file);
                    }
                }

                setOwnerCars([...ownerCars, newCar]);
                toast.success('Нове авто додано разом із пакетом фотографій! 📸');
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

    const handlePasswordSubmit = async (e) => {
            e.preventDefault();

            if (passwordForm.newPassword !== passwordForm.confirmPassword) {
                toast.warning("Паролі не збігаються! 🛑");
                return;
            }

            try {
                setIsChangingPassword(true);
                // user.id — це Keycloak ID поточного залогіненого юзера
                await userService.changePasswordByKeycloakId(user.id, passwordForm.newPassword);

                toast.success("Пароль успішно оновлено! 🔐");
                setPasswordForm({ newPassword: '', confirmPassword: '' }); // Очищаємо поля
            } catch (err) {
                console.error("Помилка зміни пароля:", err);
                toast.error(err.response?.data?.message || "Не вдалося змінити пароль.");
            } finally {
                setIsChangingPassword(false);
            }
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
                                    <td>
                                        <SecureImage
                                            src={`/car/v1/${car.id}/images/main`}
                                            alt={`${car.brand} ${car.model}`}
                                            style={{ width: '60px', height: '40px', borderRadius: '4px' }}
                                        />
                                    </td>
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
            const filteredBookings = ownerBookings.filter(ob => {
                if (ownerBookingFilter === 'ALL') return true;
                return ob.status === ownerBookingFilter;
            }).sort((a, b) => b.id - a.id);

            return (
                <>
                    <h2 className={styles.tabTitle}>📋 Сесії оренди вашого автопарку</h2>
                    <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', alignItems: 'center' }}>
                        <span style={{ fontSize: '14px', fontWeight: 'bold', color: '#555' }}>Фільтр статусів:</span>
                        <select
                            value={ownerBookingFilter}
                            onChange={(e) => setOwnerBookingFilter(e.target.value)}
                            style={{ padding: '6px 12px', borderRadius: '4px', border: '1px solid #ccc', background: '#fff', fontSize: '14px', cursor: 'pointer' }}
                        >
                            <option value="ALL">Всі замовлення</option>
                            <option value="CREATED">CREATED</option>
                            <option value="CONFIRMED">CONFIRMED</option>
                            <option value="COMPLETED">COMPLETED</option>
                            <option value="CANCELLED">CANCELLED</option>
                        </select>
                    </div>

                    <table className={styles.historyTable}>
                        <thead>
                        <tr><th>ID</th><th>Транспорт</th><th>Період активності</th><th>Загальна вартість</th><th>Статус</th></tr>
                        </thead>
                        <tbody>
                        {filteredBookings.map(ob => (
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

                    {/* 👑 Визначна позначка загальної готовності профілю */}
                    <div style={{ padding: '15px', borderRadius: '8px', marginBottom: '20px', background: isProfileVerified ? '#f6ffed' : '#fff1f0', border: isProfileVerified ? '1px solid #b7eb8f' : '1px solid #ffccc7', display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <span style={{ fontSize: '24px' }}>{isProfileVerified ? '✅' : '🛑'}</span>
                        <div>
                            <strong style={{ color: isProfileVerified ? '#389e0d' : '#cf1322', fontSize: '15px' }}>
                                {isProfileVerified ? 'Ваш аккаунт повністю верифіковано!' : 'Потрібна верифікація документів'}
                            </strong>
                            <p style={{ margin: '4px 0 0 0', fontSize: '13px', color: '#555', lineHeight: '1.4' }}>
                                {isProfileVerified
                                    ? 'Доступ до створення бронювань та спільних поїздок повністю активовано. Щасливої дороги!'
                                    : 'Бронювання авто недоступне. Будь ласка, перейдіть у вкладку "Документи", завантажте необхідні файли та дочекайтеся перевірки адміністратором.'}
                            </p>
                        </div>
                    </div>

                    <p className={styles.greeting}>Ваша персональна знижка на оренду авто становить <span className={styles.discount} style={{color: '#28a745', fontWeight: 'bold'}}>0%</span></p>

                    <button
                        className={styles.primaryBtn}
                        onClick={() => navigate('/catalog')}
                        disabled={!isProfileVerified}
                        style={{
                            opacity: isProfileVerified ? 1 : 0.5,
                            cursor: isProfileVerified ? 'pointer' : 'not-allowed',
                            backgroundColor: isProfileVerified ? '#0056b3' : '#718096'
                        }}
                    >
                        {isProfileVerified ? 'ЗАБРОНЮВАТИ АВТО' : 'БРОНЮВАННЯ БЛОКОВАНО'}
                    </button>
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
                                <React.Fragment key={order.id}>
                                    <tr>
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


                                            <button
                                                type="button"
                                                onClick={() => toggleCoDriversDropdown(order.id)}
                                                style={{ padding: '4px 8px', backgroundColor: '#6c757d', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '11px', marginLeft: '6px' }}
                                            >
                                                {expandedBookingId === order.id ? '🔼 Сховати' : '👥 Водії'}
                                            </button>

                                            {!order.isCoDriver && (order.status === 'CREATED' || order.status === 'PENDING') && (
                                                <span style={{color: '#dc3545', cursor:'pointer', fontWeight: 'bold', marginLeft: '10px'}} onClick={() => cancelBooking(order.id)}>Скасувати</span>
                                            )}
                                        </td>
                                    </tr>



                                    {expandedBookingId === order.id && (
                                        <tr style={{ backgroundColor: '#fdfdfd' }}>
                                            <td colSpan="7" style={{ padding: '12px 20px', borderTop: 'none', borderBottom: '1px solid #dee2e6' }}>
                                                <div style={{ fontSize: '13px', color: '#333' }}>
                                                    <div style={{ fontWeight: 'bold', color: '#0056b3', marginBottom: '8px' }}>👥 Учасники цієї сесії оренди:</div>

                                                    <div style={{ display: 'flex', alignItems: 'center', gap: '15px', background: '#f8f9fa', padding: '8px 12px', borderRadius: '6px', border: '1px solid #e3e6f0', width: 'fit-content', minWidth: '450px', marginBottom: '8px' }}>
                                                        <span>🔑 <strong>Основний водій (Орендатор):</strong></span>
                                                        <span style={{ fontSize: '13px', color: '#333' }}>
                                                            {order.isCoDriver ? (
                                                                <strong>{order.userEmail || order.renterEmail || order.fullName || `Користувач #${order.userId}`}</strong>
                                                            ) : (
                                                                <span style={{ backgroundColor: '#fff3cd', color: '#856404', padding: '2px 6px', borderRadius: '4px', fontWeight: 'bold' }}>Ви</span>
                                                            )}
                                                        </span>
                                                        <span style={{
                                                            fontSize: '11px',
                                                            fontWeight: 'bold',
                                                            padding: '3px 8px',
                                                            borderRadius: '4px',
                                                            marginLeft: 'auto',
                                                            backgroundColor: '#e2f1fe',
                                                            color: '#0056b3'
                                                        }}>
                                                            ОРЕНДАТОР
                                                        </span>
                                                    </div>

                                                    {coDriversLoading ? (
                                                        <div style={{ color: '#666', fontStyle: 'italic' }}>Очікування відповіді від Booking Service... ⏳</div>
                                                    ) : bookingCoDrivers.length > 0 ? (
                                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginTop: '8px' }}>
                                                            <div style={{ fontSize: '12px', fontWeight: 'bold', color: '#555', marginBottom: '4px' }}>📋 Допущені співводії:</div>
                                                            {bookingCoDrivers.map(driver => (
                                                                <div key={driver.id} style={{ display: 'flex', alignItems: 'center', gap: '15px', background: '#fff', padding: '6px 12px', borderRadius: '6px', border: '1px solid #e3e6f0', width: 'fit-content', minWidth: '450px' }}>
                                                                    <span style={{ minWidth: '180px' }}>📧 <strong>{driver.email}</strong> {driver.userId === Number(JSON.parse(localStorage.getItem('user'))?.dbId) ? '(Ви)' : ''}</span>
                                                                    <span style={{ fontSize: '11px', color: '#555', fontFamily: 'monospace', background: '#f1f1f4', padding: '2px 6px', borderRadius: '4px' }}>
                                                                        Код: {driver.driverCode}
                                                                    </span>
                                                                    <span style={{
                                                                        fontSize: '11px',
                                                                        fontWeight: 'bold',
                                                                        padding: '3px 8px',
                                                                        borderRadius: '4px',
                                                                        marginLeft: 'auto',
                                                                        backgroundColor: driver.status === 'ACCEPTED' ? '#d4edda' : (driver.status === 'PENDING' ? '#fff3cd' : '#f8d7da'),
                                                                        color: driver.status === 'ACCEPTED' ? '#155724' : (driver.status === 'PENDING' ? '#856404' : '#721c24')
                                                                    }}>
                                                                        {driver.status}
                                                                    </span>
                                                                </div>
                                                            ))}
                                                        </div>
                                                    ) : (
                                                        <div style={{ color: '#777', fontStyle: 'italic', fontSize: '12px', marginTop: '4px' }}>До цієї поїздки ще не додано жодного стороннього водія.</div>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    )}
                                </React.Fragment>
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
                                            <button
                                                onClick={() => handleInvitationResponse(invite.id, 'accept')}
                                                disabled={!isProfileVerified}
                                                style={{
                                                    padding: '5px 10px',
                                                    marginRight: '10px',
                                                    backgroundColor: '#28a745',
                                                    color: '#fff',
                                                    border: 'none',
                                                    borderRadius: '4px',
                                                    cursor: isProfileVerified ? 'pointer' : 'not-allowed',
                                                    opacity: isProfileVerified ? 1 : 0.5
                                                }}
                                                title={!isProfileVerified ? "Пройдіть верифікацію для спільної поїздки" : ""}
                                            >
                                                Прийняти
                                            </button>
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
              const documentTypes = [
                  { type: 'PASSPORT_MAIN', label: 'Паспорт: 1-2 сторінка або ID-картка' },
                  { type: 'PASSPORT_REGISTRATION', label: 'Паспорт: прописка / витяг' },
                  { type: 'DRIVING_LICENSE', label: 'Водійське посвідчення' }
              ];



              const handleFileChange = async (type, file) => {
                  if (!file) return;
                  try {
                      setDocsLoading(true);
                      const dbId = JSON.parse(localStorage.getItem('user'))?.dbId;

                      const fileExtension = file.name.split('.').pop();
                      const safeName = `user_${dbId}_doc_${type}_${Date.now()}.${fileExtension}`;
                      const safeFile = new File([file], safeName, { type: file.type });

                      await documentService.uploadDocument(dbId, type, safeFile);
                      toast.success("Документ успішно надіслано на модерацію! 📄");

                      const meta = await documentService.getMetadata(dbId);
                      setUploadedDocs(meta || []);
                      const status = await documentService.getProfileStatus(dbId);
                      setIsProfileVerified(status);
                  } catch (err) {
                      toast.error("Не вдалося завантажити файл документа.");
                  } finally {
                      setDocsLoading(false);
                  }
              };

              return (
                  <>
                      <h2 className={styles.tabTitle}>🛡️ Центр державної верифікації профілю</h2>
                      <p className={styles.infoText}>Для відкриття можливості оренди та спільних поїздок, завантажте скани обов’язкових документів (формати PDF, PNG, JPEG).</p>

                      {docsLoading && <div style={{ color: '#0056b3', fontWeight: 'bold', marginBottom: '15px' }}>Стрімінг файлів у хмару сховища... ⏳</div>}

                      <div style={{ display: 'flex', flexDirection: 'column', gap: '15px', marginTop: '20px' }}>
                          {documentTypes.map(doc => {
                              const meta = (uploadedDocs || []).find(d => d.documentType === doc.type);
                              return (
                                  <div key={doc.type} style={{ padding: '15px', background: '#fff', borderRadius: '8px', border: '1px solid #eef0f2', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                      <div>
                                          <div style={{ fontSize: '14px', color: '#222', fontWeight: 'bold' }}>{doc.label}</div>
                                          {meta ? (
                                              <div style={{ marginTop: '5px', fontSize: '12px', color: '#666' }}>
                                                  📄 {meta.originalFileName} <br/>
                                                  📅 Завантажено: {meta.uploadedAt?.split('T')[0]}
                                              </div>
                                          ) : (
                                              <div style={{ marginTop: '5px', fontSize: '12px', color: '#999', fontStyle: 'italic' }}>Файл відсутній</div>
                                          )}
                                      </div>

                                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                          {meta && (
                                              <button
                                                  onClick={() => setPreviewDoc(meta)}
                                                  style={{ padding: '6px 10px', background: '#e2f1fe', color: '#0056b3', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }}
                                              >
                                                  👁️ Огляд
                                              </button>
                                          )}

                                          <span style={{ fontSize: '11px', fontWeight: 'bold', padding: '4px 10px', borderRadius: '4px', backgroundColor: meta ? (meta.isVerified ? '#d4edda' : '#fff3cd') : '#f5f5f5', color: meta ? (meta.isVerified ? '#155724' : '#856404') : '#777' }}>
                                              {meta ? (meta.isVerified ? '● ВЕРИФІКОВАНО' : '● НА ПЕРЕВІРЦІ') : 'ОБОВ\'ЯЗКОВО'}
                                          </span>

                                          <input type="file" id={`file-${doc.type}`} accept="image/png, image/jpeg, image/jpg, application/pdf" onChange={(e) => handleFileChange(doc.type, e.target.files[0])} style={{ display: 'none' }} />
                                          <button onClick={() => document.getElementById(`file-${doc.type}`).click()} style={{ padding: '6px 12px', border: '1px solid #0056b3', background: '#f8fbff', color: '#0056b3', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }}>
                                              {meta ? 'Оновити 🔄' : 'Обрати файл 📁'}
                                          </button>
                                      </div>
                                  </div>
                              );
                          })}
                      </div>

                      {previewDoc && (
                          <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.7)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000 }} onClick={() => setPreviewDoc(null)}>
                              <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', maxWidth: '80%', maxHeight: '80%', overflow: 'auto' }} onClick={e => e.stopPropagation()}>
                                  <h3 style={{marginTop: 0}}>Перегляд: {previewDoc.originalFileName}</h3>
                                  {previewDoc.contentType.includes('image') ? (
                                      <SecureImage src={`/document/v1/${previewDoc.id}/download`} style={{ maxWidth: '100%', maxHeight: '500px' }} />
                                  ) : (
                                      <div style={{ padding: '20px', textAlign: 'center' }}>
                                          <p>Це PDF документ.</p>
                                          <a href={`http://localhost:8100/document/v1/${previewDoc.id}/download`} target="_blank" rel="noreferrer" style={{ color: '#0056b3', fontWeight: 'bold' }}>Відкрити файл ↗</a>
                                      </div>
                                  )}
                                  <button onClick={() => setPreviewDoc(null)} style={{ marginTop: '15px', padding: '8px 16px', cursor: 'pointer' }}>Закрити</button>
                              </div>
                          </div>
                      )}
                  </>
              );
          }

        if (activeTab === 'analytics') {
                    if (analyticsLoading) return <div style={{ textAlign: 'center', padding: '40px' }}>Обчислення фінансових метрик... ⏳</div>;
                    if (!ownerAnalytics) return <div style={{ textAlign: 'center', padding: '40px', color: '#dc3545' }}>Не вдалося завантажити аналітичні дані.</div>;

                    const ukrMonths = { 1: 'Січ', 2: 'Лют', 3: 'Бер', 4: 'Квіт', 5: 'Трав', 6: 'Черв', 7: 'Лип', 8: 'Серп', 9: 'Верес', 10: 'Жовт', 11: 'Листоп', 12: 'Груд' };


                    const maxRevenue = ownerAnalytics.monthlyRevenue?.length > 0
                        ? Math.max(...ownerAnalytics.monthlyRevenue.map(m => m[1]))
                        : 100;

                    return (
                        <>
                            <h2 className={styles.tabTitle}>📊 Фінансовий дашборд автопарку</h2>


                            <div className={styles.statsContainer} style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '15px', marginBottom: '30px' }}>
                                <div className={styles.statCard} style={{ background: '#f8fbff', border: '1px solid #d6e4ff' }}>
                                    <div className={styles.statCardTitle} style={{ color: '#555', fontSize: '13px' }}>Всього машин</div>
                                    <div className={styles.statCardValue} style={{ fontSize: '24px', color: '#0056b3' }}>{ownerAnalytics.totalCars}</div>
                                </div>
                                <div className={styles.statCard}>
                                    <div className={styles.statCardTitle} style={{ color: '#555', fontSize: '13px' }}>Загальні бронювання</div>
                                    <div className={styles.statCardValue} style={{ fontSize: '24px' }}>{ownerAnalytics.totalBookings}</div>
                                </div>
                                <div className={styles.statCard} style={{ background: '#f6ffed', border: '1px solid #b7eb8f' }}>
                                    <div className={styles.statCardTitle} style={{ color: '#555', fontSize: '13px' }}>Завершені сесії</div>
                                    <div className={styles.statCardValue} style={{ fontSize: '24px', color: '#389e0d' }}>{ownerAnalytics.completedBookings}</div>
                                </div>
                                <div className={styles.statCard} style={{ background: '#fff7e6', border: '1px solid #ffd591' }}>
                                    <div className={styles.statCardTitle} style={{ color: '#555', fontSize: '13px' }}>Загальний виторг</div>
                                    <div className={styles.statCardValue} style={{ fontSize: '24px', color: '#d46b08' }}>{ownerAnalytics.totalRevenue} €</div>
                                </div>
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1.5fr 1fr', gap: '20px', marginTop: '20px' }}>

                                <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', border: '1px solid #eee' }}>
                                    <h3 style={{ margin: '0 0 20px 0', fontSize: '16px', color: '#333' }}>📈 Динаміка доходів за місяцями</h3>
                                    {ownerAnalytics.monthlyRevenue?.length > 0 ? (
                                        <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-around', height: '200px', paddingBottom: '20px', borderBottom: '2px solid #ddd' }}>
                                            {ownerAnalytics.monthlyRevenue.map(([monthNum, revenue], idx) => {
                                                const barHeight = (revenue / maxRevenue) * 150; // Динамічна висота стовпчика
                                                return (
                                                    <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', width: '60px', position: 'relative' }}>
                                                        <span style={{ fontSize: '12px', fontWeight: 'bold', marginBottom: '5px', color: '#0056b3' }}>{revenue}€</span>
                                                        <div style={{ height: `${barHeight}px`, width: '35px', background: 'linear-gradient(180deg, #3ba4f6 0%, #0056b3 100%)', borderRadius: '4px 4px 0 0', transition: 'height 0.3s ease' }}></div>
                                                        <span style={{ position: 'absolute', bottom: '-22px', fontSize: '12px', color: '#666', fontWeight: '500' }}>{ukrMonths[monthNum] || monthNum}</span>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    ) : (
                                        <div style={{ textAlign: 'center', color: '#999', paddingTop: '60px' }}>Дані про щомісячні доходи відсутні.</div>
                                    )}
                                </div>


                                <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', border: '1px solid #eee' }}>
                                    <h3 style={{ margin: '0 0 15px 0', fontSize: '16px', color: '#333' }}>🚗 Завантаженість парку (Поточний тиждень)</h3>
                                    {ownerAnalytics.weeklyLoad?.length > 0 ? (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '15px' }}>
                                            {ownerAnalytics.weeklyLoad.map(([dateStr, loadCount], idx) => (
                                                <div key={idx} style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                                    <span style={{ width: '80px', fontSize: '12px', color: '#555' }}>{dateStr}</span>
                                                    <div style={{ flex: 1, background: '#f5f5f5', height: '14px', borderRadius: '4px', overflow: 'hidden' }}>
                                                        <div style={{ width: `${Math.min(loadCount * 20, 100)}%`, background: '#28a745', height: '100%' }}></div>
                                                    </div>
                                                    <span style={{ fontSize: '12px', fontWeight: 'bold', width: '20px' }}>{loadCount}</span>
                                                </div>
                                            ))}
                                        </div>
                                    ) : (
                                        <div style={{ textAlign: 'center', color: '#777', padding: '40px 10px', background: '#fafafa', borderRadius: '6px', border: '1px dashed #ccc', marginTop: '20px' }}>
                                            <div style={{ fontSize: '24px', marginBottom: '5px' }}>💤</div>
                                            <div style={{ fontSize: '13px', fontStyle: 'italic' }}>Всі 9 машин власника відпочивають. На цьому тижні активних сесій оренди немає.</div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        </>
                    );
                }

        if (activeTab === 'profile') {
                    return (
                        <>
                            <h2 className={styles.tabTitle}>Персональні дані</h2>

                            {/* Форма 1: Зміна імені та прізвища */}
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
                                <button type="submit" className={styles.primaryBtn} style={{marginBottom: '0'}}>Зберегти зміни профілю</button>
                            </form>

                            <hr style={{ border: 'none', borderTop: '1px solid #dee2e6', margin: '30px 0' }} />

                            {/* Форма 2: Безпека та зміна пароля */}
                            <h3 className={styles.sectionSubtitle} style={{ marginBottom: '15px', fontSize: '18px', color: '#333' }}>🔒 Безпека облікового запису</h3>
                            <form onSubmit={handlePasswordSubmit}>
                                <div className={styles.formGrid} style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px', marginBottom: '20px'}}>
                                    <div className={styles.inputGroup}>
                                        <label style={{display: 'block', marginBottom: '5px'}}>Новий пароль</label>
                                        <input
                                            type="password"
                                            required
                                            minLength="6"
                                            placeholder="Введіть новий пароль"
                                            value={passwordForm.newPassword}
                                            onChange={(e) => setPasswordForm({...passwordForm, newPassword: e.target.value})}
                                            style={{width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc'}}
                                        />
                                    </div>
                                    <div className={styles.inputGroup}>
                                        <label style={{display: 'block', marginBottom: '5px'}}>Повторіть новий пароль</label>
                                        <input
                                            type="password"
                                            required
                                            placeholder="Повторіть пароль"
                                            value={passwordForm.confirmPassword}
                                            onChange={(e) => setPasswordForm({...passwordForm, confirmPassword: e.target.value})}
                                            style={{width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ccc'}}
                                        />
                                    </div>
                                </div>
                                <button type="submit" className={styles.primaryBtn} style={{ backgroundColor: '#28a745' }} disabled={isChangingPassword}>
                                    {isChangingPassword ? 'Оновлення...' : 'ЗМІНИТИ ПАРОЛЬ'}
                                </button>
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
                                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Фотографії автомобіля (Можна декілька за раз)</label>
                                <input
                                    type="file"
                                    multiple
                                    accept="image/png, image/jpeg, image/jpg"
                                    onChange={(e) => {
                                        if (e.target.files) {
                                            const filesArray = Array.from(e.target.files);
                                            setSelectedFiles(prevFiles => {
                                                const uniqueNewFiles = filesArray.filter(
                                                    newFile => !prevFiles.some(prevFile => prevFile.name === newFile.name && prevFile.size === newFile.size)
                                                );
                                                return [...prevFiles, ...uniqueNewFiles];
                                            });
                                        }
                                    }}
                                    style={{ width: '100%', padding: '8px', border: '1px dashed #0056b3', borderRadius: '4px', background: '#f8fbff', cursor: 'pointer' }}
                                />

                                {selectedFiles.length > 0 && (
                                    <div style={{ marginTop: '12px', maxHeight: '130px', overflowY: 'auto', background: '#fff', padding: '10px', borderRadius: '6px', border: '1px solid #ddd' }}>
                                        <div style={{ fontSize: '12px', fontWeight: 'bold', marginBottom: '6px', color: '#333' }}>Обрані медіафайли ({selectedFiles.length}):</div>
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
                                            {selectedFiles.map((file, i) => (
                                                <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#f1f3f9', padding: '4px 8px', borderRadius: '4px', fontSize: '12px' }}>
                                                    <span style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', maxWidth: '85%' }}>📷 {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)</span>
                                                    <button
                                                        type="button"
                                                        onClick={() => setSelectedFiles(selectedFiles.filter((_, idx) => idx !== i))}
                                                        style={{ background: 'none', border: 'none', color: '#dc3545', fontWeight: 'bold', cursor: 'pointer', fontSize: '14px', padding: '0 4px' }}
                                                        title="Прибрати зі списку"
                                                    >
                                                        ✕
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '20px' }}>
                                <button type="button" onClick={() => setShowCarModal(false)} style={{ padding: '10px 20px', background: '#ccc', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Скасувати</button>
                                <button type="submit" style={{ padding: '10px 20px', background: '#0056b3', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Зберегти</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    )
};

export default UserProfilePage;
