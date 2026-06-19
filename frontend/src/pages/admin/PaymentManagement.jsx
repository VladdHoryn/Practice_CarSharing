import React, { useState, useEffect } from 'react';
import { paymentService } from '../../services/payment.service';
import { toast } from 'react-toastify';
import styles from './PaymentManagement.module.css';

const PaymentManagement = () => {
    const [payments, setPayments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [statusFilter, setStatusFilter] = useState('ALL');

    const fetchPayments = async () => {
        try {
            setLoading(true);
            const data = await paymentService.getAllPayments();
            setPayments(data || []);
        } catch (err) {
            console.error('Помилка завантаження платежів:', err);
            toast.error('Не вдалося отримати дані транзакцій.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPayments();
    }, []);

    const handleMarkAsPaid = async (bookingId) => {
        if (!window.confirm(`Підтвердити платіж для бронювання #BK-${bookingId} як CONFIRMED вручну?`)) return;
        try {
            await paymentService.changePaymentStatus(bookingId, 'CONFIRMED');
            toast.success(`Трансляцію оплати успішно підтверджено! 💳`);
            fetchPayments();
        } catch (err) {
            toast.error('Не вдалося оновити статус фінансової операції.');
        }
    };

    const filteredPayments = payments.filter(p => statusFilter === 'ALL' || p.status === statusFilter);

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>💳 Фінансовий моніторинг транзакцій</h1>
            <div className={styles.filterBar}>
                <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className={styles.select}>
                    <option value="ALL">Всі транзакції</option>
                    <option value="PAID">PAID</option>
                    <option value="PENDING">PENDING</option>
                    <option value="FAILED">FAILED</option>
                </select>
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
                            <th>Метод</th>
                            <th>Статус</th>
                            <th>Адмін-дії</th>
                        </tr>
                        </thead>
                        <tbody>
                        {filteredPayments.map((p) => (
                            <tr key={p.id}>
                                <td>#TX-{p.id}</td>
                                <td>#BK-{p.bookingId}</td>
                                <td><strong>{p.amount} {p.currency}</strong></td>
                                <td>{p.method}</td>
                                <td><span className={`${styles.statusBadge} ${styles[p.status?.toLowerCase() || 'pending']}`}>{p.status}</span></td>
                                <td>
                                    {p.status !== 'PAID' && p.status !== 'CONFIRMED' ? (
                                        <button onClick={() => handleMarkAsPaid(p.bookingId)} className={styles.actionBtn}>
                                            ✅ Mark as Paid
                                        </button>
                                    ) : (
                                        <span style={{ fontSize: '13px', color: '#94a3b8', fontStyle: 'italic' }}>Проведено</span>
                                    )}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default PaymentManagement;
