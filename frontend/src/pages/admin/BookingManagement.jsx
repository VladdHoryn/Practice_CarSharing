import React, { useState, useEffect } from 'react';
import { bookingService } from '../../services/booking.service';
import { toast } from 'react-toastify';
import styles from './BookingManagement.module.css';

const BookingManagement = () => {
    const [bookings, setBookings] = useState([]);
    const [allInvitations, setAllInvitations] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedBooking, setSelectedBooking] = useState(null);

    const [statusFilter, setStatusFilter] = useState('ALL');
    const [sortBy, setSortBy] = useState('id');

    const fetchAllBookings = async () => {
        try {
            setLoading(true);
            const data = await bookingService.getAllBookings();
            setBookings(data || []);

            // 👑 KILLER FEATURE: Отримання всіх інвайтів співводіїв для моніторингу адміном
            if (bookingService.getAllInvitations) {
                const invites = await bookingService.getAllInvitations();
                setAllInvitations(invites || []);
            }
        } catch (err) {
            console.error('Помилка завантаження бронювань:', err);
            toast.error('Не вдалося завантажити бронювання з сервера.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAllBookings();
    }, []);

    const handleForceStatusChange = async (bookingId, newStatus) => {
        try {
            await bookingService.changeBookingStatus(bookingId, newStatus);
            toast.success(`Статус бронювання #${bookingId} примусово змінено на ${newStatus}`);
            fetchAllBookings();

            if (selectedBooking && selectedBooking.id === bookingId) {
                setSelectedBooking({ ...selectedBooking, status: newStatus });
            }
        } catch (err) {
            console.error("Помилка зміни статусу букінгу:", err);
            toast.error('Не вдалося оновити статус на сервері.');
        }
    };

    const filteredAndSortedBookings = bookings
        .filter(b => statusFilter === 'ALL' || b.status === statusFilter)
        .sort((a, b) => {
            if (sortBy === 'totalPrice') return b.totalPrice - a.totalPrice;
            return b.id - a.id;
        });

    // 👑 Фільтрація запрошень водіїв для конкретного обраного бронювання
    const currentBookingDrivers = allInvitations.filter(
        invite => invite.bookingId === selectedBooking?.id
    );

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>📅 Керування сесіями оренди</h1>

            <div style={{ marginBottom: '15px', display: 'flex', gap: '15px' }}>
                <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className={styles.statusSelect}>
                    <option value="ALL">Всі статуси</option>
                    <option value="CREATED">CREATED</option>
                    <option value="CONFIRMED">CONFIRMED</option>
                    <option value="CANCELLED">CANCELLED</option>
                    <option value="COMPLETED">COMPLETED</option>
                </select>
                <select value={sortBy} onChange={(e) => setSortBy(e.target.value)} className={styles.statusSelect}>
                    <option value="id">Сортувати за ID</option>
                    <option value="totalPrice">Сортувати за ціною</option>
                </select>
            </div>

            {loading ? (
                <div className={styles.loader}>Завантаження історії бронювань... ⏳</div>
            ) : (
                <div className={styles.tableWrapper}>
                    <table className={styles.table}>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Клієнт (ID)</th>
                            <th>Автомобіль (ID)</th>
                            <th>Період оренди</th>
                            <th>Сума</th>
                            <th>Статус</th>
                            <th>Дії</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredAndSortedBookings.map((b) => (
                            <tr key={b.id} className={styles.tableRow} onClick={() => setSelectedBooking(b)}>
                                <td>#{b.id}</td>
                                <td>{b.userId}</td>
                                <td><strong>Авто #{b.carId}</strong></td>
                                <td>
                                    {b.startDate?.split('T')[0]} — {b.endDate?.split('T')[0]}
                                </td>
                                <td><strong>{b.totalPrice}€</strong></td>
                                <td>
                                    <span className={`${styles.statusBadge} ${styles[b.status?.toLowerCase() || 'created']}`}>
                                        {b.status}
                                    </span>
                                </td>
                                <td onClick={(e) => e.stopPropagation()}>
                                    <select
                                        value={b.status}
                                        onChange={(e) => handleForceStatusChange(b.id, e.target.value)}
                                        className={styles.statusSelect}
                                    >
                                        <option value="CREATED">CREATED</option>
                                        <option value="CONFIRMED">CONFIRMED</option>
                                        <option value="CANCELLED">CANCELLED</option>
                                        <option value="COMPLETED">COMPLETED</option>
                                    </select>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}

            {selectedBooking && (
                <div className={styles.drawerOverlay} onClick={() => setSelectedBooking(null)}>
                    <div className={styles.drawer} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.drawerHeader}>
                            <h2>Деталі замовлення #{selectedBooking.id}</h2>
                            <button className={styles.closeBtn} onClick={() => setSelectedBooking(null)}>✖</button>
                        </div>
                        <div className={styles.drawerContent}>
                            <div className={styles.infoGroup}><label>Головний користувач системи (ID)</label><p>{selectedBooking.userId}</p></div>
                            <div className={styles.infoGroup}><label>Обраний транспорт (ID)</label><p>{selectedBooking.carId}</p></div>
                            <div className={styles.infoGroup}>
                                <label>Терміни активності</label>
                                <p>З: {selectedBooking.startDate?.replace('T', ' ')}</p>
                                <p>По: {selectedBooking.endDate?.replace('T', ' ')}</p>
                            </div>
                            <div className={styles.infoGroup}><label>Фінансова частина</label><span className={styles.drawerPrice}>{selectedBooking.totalPrice} €</span></div>

                            {/* 👑 KILLER FEATURE UI: Перегляд списку всіх водіїв для Адміністратора */}
                            <div style={{ marginTop: '20px', paddingTop: '20px', borderTop: '1px solid #eee' }}>
                                <h3 style={{ fontSize: '16px', marginBottom: '10px' }}>👥 Зареєстровані водії за договором:</h3>
                                <div style={{ fontSize: '14px', marginBottom: '8px' }}>
                                    • <span style={{ fontWeight: 'bold' }}>ID #{selectedBooking.userId}</span> (Головний водій / Орендар)
                                </div>
                                {currentBookingDrivers.length > 0 ? (
                                    currentBookingDrivers.map((driver) => (
                                        <div key={driver.id} style={{ fontSize: '14px', padding: '6px 0', borderBottom: '1px dashed #f0f0f0' }}>
                                            • {driver.email} [Код: <code>{driver.driverCode}</code>] —
                                            <span style={{
                                                marginLeft: '6px',
                                                fontWeight: 'bold',
                                                color: driver.status === 'ACCEPTED' ? '#28a745' : (driver.status === 'PENDING' ? '#f39c12' : '#dc3545')
                                            }}>
                                                {driver.status}
                                            </span>
                                        </div>
                                    ))
                                ) : (
                                    <p style={{ fontSize: '12px', color: '#888', fontStyle: 'italic' }}>Додаткових співводіїв не запрошено.</p>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BookingManagement;
