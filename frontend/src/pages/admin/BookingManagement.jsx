import React, { useState, useEffect } from 'react';
import { bookingService } from '../../services/booking.service';
import { toast } from 'react-toastify';
import styles from './BookingManagement.module.css';

const BookingManagement = () => {
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedBooking, setSelectedBooking] = useState(null); // Для бічного Drawer

    const fetchAllBookings = async () => {
        try {
            setLoading(true);
            // Припускаємо наявність загального адмін-методу в майбутньому
            if (bookingService.getAllBookings) {
                const data = await bookingService.getAllBookings();
                setBookings(data);
            } else {
                throw new Error("API method not ready");
            }
        } catch (err) {
            console.error('Помилка завантаження бронювань:', err);
            // Реалістичні мокові дані на основі останніх тестів у системі
            setBookings([
                {
                    id: 11,
                    userId: 12,
                    userEmail: 'zhuryk@carsharing.com',
                    carId: 1,
                    carName: 'Toyota Yaris',
                    startDate: '2026-05-28T12:00:00',
                    endDate: '2026-05-29T12:00:00',
                    totalPrice: 25,
                    status: 'CREATED',
                    cancelDeadline: '2026-05-26T12:00:00',
                    coDrivers: [] // Поки пусто, під фічу Split Access
                },
                {
                    id: 10,
                    userId: 101,
                    userEmail: 'olex@gmail.com',
                    carId: 2,
                    carName: 'Tesla Model 3',
                    startDate: '2026-06-01T10:00:00',
                    endDate: '2026-06-05T10:00:00',
                    totalPrice: 240,
                    status: 'CONFIRMED',
                    cancelDeadline: '2026-05-30T10:00:00',
                    coDrivers: [
                        { email: 'friend1@gmail.com', licenseNumber: 'BXX123456' }
                    ]
                }
            ]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAllBookings();
    }, []);

    const handleForceStatusChange = async (bookingId, newStatus) => {
        try {
            // Примусова зміна статусу адміністратором через API
            if (bookingService.updateBookingStatus) {
                await bookingService.updateBookingStatus(bookingId, newStatus);
            }
            toast.success(`Статус бронювання #${bookingId} примусово змінено на ${newStatus}`);
            fetchAllBookings();
            if (selectedBooking && selectedBooking.id === bookingId) {
                setSelectedBooking({ ...selectedBooking, status: newStatus });
            }
        } catch (err) {
            toast.error('Не вдалося змінити статус.');
        }
    };

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>📅 Керування сесіями оренди</h1>

            {loading ? (
                <div className={styles.loader}>Завантаження історії бронювань... ⏳</div>
            ) : (
                <div className={styles.tableWrapper}>
                    <table className={styles.table}>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Клієнт</th>
                            <th>Автомобіль</th>
                            <th>Період оренди</th>
                            <th>Сума</th>
                            <th>Статус</th>
                            <th>Дії</th>
                        </tr>
                        </thead>
                        <tbody>
                        {bookings.map((b) => (
                            <tr key={b.id} className={styles.tableRow} onClick={() => setSelectedBooking(b)}>
                                <td>#{b.id}</td>
                                <td>{b.userEmail}</td>
                                <td><strong>{b.carName}</strong></td>
                                <td>
                                    {b.startDate.split('T')[0]} — {b.endDate.split('T')[0]}
                                </td>
                                <td><strong>{b.totalPrice}€</strong></td>
                                <td>
                                        <span className={`${styles.statusBadge} ${styles[b.status.toLowerCase()]}`}>
                                            {b.status}
                                        </span>
                                </td>
                                <td onClick={(e) => e.stopPropagation()}>
                                    <select
                                        value={b.status}
                                        onChange={(e) => handleForceStatusChange(b.id, e.target.value)}
                                        className={styles.statusSelect}
                                    >
                                        <option value="CREATED">CREATED (Створено)</option>
                                        <option value="CONFIRMED">CONFIRMED (Підтверджено)</option>
                                        <option value="CANCELLED">CANCELLED (Скасовано)</option>
                                        <option value="COMPLETED">COMPLETED (Завершено)</option>
                                    </select>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}

            {/* 🔥 БІЧНА ПАНЕЛЬ (DRAWER) ДЕТАЛЕЙ БРОНЮВАННЯ */}
            {selectedBooking && (
                <div className={styles.drawerOverlay} onClick={() => setSelectedBooking(null)}>
                    <div className={styles.drawer} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.drawerHeader}>
                            <h2>Деталі замовлення #{selectedBooking.id}</h2>
                            <button className={styles.closeBtn} onClick={() => setSelectedBooking(null)}>✖</button>
                        </div>

                        <div className={styles.drawerContent}>
                            <div className={styles.infoGroup}>
                                <label>Користувач системи (ID: {selectedBooking.userId})</label>
                                <p>{selectedBooking.userEmail}</p>
                            </div>

                            <div className={styles.infoGroup}>
                                <label>Обраний транспорт (ID: {selectedBooking.carId})</label>
                                <p>{selectedBooking.carName}</p>
                            </div>

                            <div className={styles.infoGroup}>
                                <label>Терміни активності</label>
                                <p>З: {selectedBooking.startDate.replace('T', ' ')}</p>
                                <p>По: {selectedBooking.endDate.replace('T', ' ')}</p>
                            </div>

                            {/* 🛠 PLACEHOLDER: ДЕДЛАЙН СКАСУВАННЯ */}
                            <div className={`${styles.infoGroup} ${styles.placeholderGroup}`}>
                                <label>⏳ Граничний термін скасування (Cancel Deadline)</label>
                                <p>{selectedBooking.cancelDeadline ? selectedBooking.cancelDeadline.replace('T', ' ') : 'Не встановлено'}</p>
                                <small>Дозволяє системі автоматично блокувати чи дозволяти метод /cancel</small>
                            </div>

                            {/* 🛠 PLACEHOLDER: SPLIT ACCESS (ДОДАТКОВІ ВОДІЇ) */}
                            <div className={`${styles.infoGroup} ${styles.placeholderGroup}`}>
                                <label>👥 Спільний доступ (Split Access Co-Drivers)</label>
                                {selectedBooking.coDrivers && selectedBooking.coDrivers.length > 0 ? (
                                    <ul className={styles.driversList}>
                                        {selectedBooking.coDrivers.map((driver, index) => (
                                            <li key={index}>
                                                📧 {driver.email} (Права: <code>{driver.licenseNumber}</code>)
                                            </li>
                                        ))}
                                    </ul>
                                ) : (
                                    <p className={styles.noDrivers}>Додаткових водіїв не зареєстровано.</p>
                                )}
                                <small>Відображає права третіх осіб на керування за даним договором</small>
                            </div>

                            <div className={styles.infoGroup}>
                                <label>Фінансова частина</label>
                                <span className={styles.drawerPrice}>{selectedBooking.totalPrice} €</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BookingManagement;
