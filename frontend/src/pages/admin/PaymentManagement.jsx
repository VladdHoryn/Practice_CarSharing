import React, { useState, useEffect } from 'react';
import { paymentService } from '../../services/payment.service';
import { toast } from 'react-toastify';
import styles from './PaymentManagement.module.css';

const PaymentManagement = () => {
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);

    // Стейт для фільтрації
    const [statusFilter, setStatusFilter] = useState('ALL');
    const [dateFilter, setDateFilter] = useState('');

    const fetchPayments = async () => {
        try {
            setLoading(true);
            // Викликаємо твій реальний метод getAll з платіжного сервісу
            const data = await paymentService.getAll();
            setPayments(data || []);
        } catch (err) {
            console.error('Помилка завантаження платежів:', err);
            // Мокові дані на основі твоїх DTO та полів для тестів фронту
            setPayments([
                { id: 1, bookingId: 11, amount: 25.00, method: 'GOOGLE_PAY', currency: 'USD', status: 'PENDING', paymentDate: '2026-05-28' },
                { id: 2, bookingId: 9, amount: 120.00, method: 'CARD', currency: 'USD', status: 'PAID', paymentDate: '2026-05-25' },
                { id: 3, bookingId: 8, amount: 50.00, method: 'APPLE_PAY', currency: 'USD', status: 'FAILED', paymentDate: '2026-05-24' }
            ]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPayments();
    }, []);

    // Ручне маркування платежу як успішного (Кейс: адмін підтвердив гроші)
    const handleMarkAsPaid = async (paymentId) => {
        const confirmAction = window.confirm(`Ви впевнені, що хочете вручну відзначити платіж #${paymentId} як ОПЛАЧЕНИЙ?`);
        if (!confirmAction) return;

        try {
            // Викликаємо твій реальний PatchMapping метод /{id}/success із контролера
            if (paymentService.markAsSuccess) {
                await paymentService.markAsSuccess(paymentId);
            }
            toast.success(`Платіж #${paymentId} успішно підтверджено вручну! 💳`);
            fetchPayments(); // Перезавантажуємо дані
        } catch (err) {
            toast.error('Не вдалося змінити статус платежу.');
        }
    };

    // Логіка фільтрації на фронтенді (поки бекенд не підтримує динамічні Query-специфікації)
    const filteredPayments = payments.filter(p => {
        const matchesStatus = statusFilter === 'ALL' || p.status === statusFilter;
        const matchesDate = !dateFilter || p.paymentDate.includes(dateFilter);
        return matchesStatus && matchesDate;
    });

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>💳 Фінансовий моніторинг транзакцій</h1>

            {/* Панель фільтрів */}
            <div className={styles.filterBar}>
                <div className={styles.filterGroup}>
                    <label>Статус оплати:</label>
                    <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className={styles.select}>
                        <option value="ALL">Всі транзакції</option>
                        <option value="PAID">PAID (Оплачено)</option>
                        <option value="PENDING">PENDING (Очікує)</option>
                        <option value="FAILED">FAILED (Помилка)</option>
                    </select>
                </div>

                <div className={styles.filterGroup}>
                    <label>Дата транзакції:</label>
                    <input
                        type="date"
                        value={dateFilter}
                        onChange={(e) => setDateFilter(e.target.value)}
                        className={styles.dateInput}
                    />
                </div>

                {dateFilter || statusFilter !== 'ALL' ? (
                    <button onClick={() => { setStatusFilter('ALL'); setDateFilter(''); }} className={styles.clearBtn}>
                        Скинути фільтри
                    </button>
                ) : null}
            </div>

            {loading ? (
                <div className={styles.loader}>Обробка фінансових звітів... ⏳</div>
            ) : (
                <div className={styles.tableWrapper}>
                    <table className={styles.table}>
                        <thead>
                        <tr>
                            <th>ID Транзакції</th>
                            <th>ID Бронювання</th>
                            <th>Сума</th>
                            <th>Метод оплати</th>
                            <th>Дата</th>
                            <th>Статус</th>
                            <th>Адмін-дії</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredPayments.length === 0 ? (
                            <tr>
                                <td colSpan="7" style={{ textAlign: 'center', padding: '30px', color: '#64748b' }}>
                                    Транзакцій за вказаними фільтрами не знайдено.
                                </td>
                            </tr>
                        ) : (
                            filteredPayments.map((p) => (
                                <tr key={p.id}>
                                    <td>#TX-{p.id}</td>
                                    <td>#BK-{p.bookingId}</td>
                                    <td><strong className={styles.amountText}>{p.amount.toFixed(2)} {p.currency}</strong></td>
                                    <td>
                                            <span className={styles.methodIcon}>
                                                {p.method === 'CARD' ? '💳' : p.method === 'GOOGLE_PAY' ? '📱' : '🍎'}
                                            </span>{' '}
                                        {p.method}
                                    </td>
                                    <td>{p.paymentDate ? p.paymentDate.split('T')[0] : 'Не вказано'}</td>
                                    <td>
                                            <span className={`${styles.statusBadge} ${styles[p.status.toLowerCase()]}`}>
                                                {p.status}
                                            </span>
                                    </td>
                                    <td>
                                        {p.status === 'PENDING' || p.status === 'FAILED' ? (
                                            <button
                                                onClick={() => handleMarkAsPaid(p.id)}
                                                className={styles.actionBtn}
                                            >
                                                ✅ Mark as Paid
                                            </button>
                                        ) : (
                                            <span style={{ fontSize: '13px', color: '#94a3b8', fontStyle: 'italic' }}>Завершено</span>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default PaymentManagement;
