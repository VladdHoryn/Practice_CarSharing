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

    const handleMarkAsPaid = async (payment) => {
        if (!window.confirm(`Підтвердити платіж #TX-${payment.id} для бронювання #BK-${payment.bookingId} вручну?`)) return;
        try {
            await paymentService.changePaymentStatus(payment.bookingId, 'COMPLETED');

            toast.success(`Трансляцію оплати успішно підтверджено! 💳`);

            setPayments(prevPayments =>
                prevPayments.map(p =>
                    p.id === payment.id ? { ...p, status: 'SUCCESS' } : p
                )
            );
        } catch (err) {
            console.error("Помилка фінансової операції:", err);
            const serverMessage = err.response?.data?.message || err.response?.data || err.message;
            toast.error(`Помилка сервера: ${serverMessage}`);
        }
    };

    const filteredPayments = payments.filter(p => statusFilter === 'ALL' || p.status === statusFilter);

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>💳 Фінансовий моніторинг транзакцій</h1>
            <div className={styles.filterBar}>
                <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} className={styles.select}>
                    <option value="ALL">Всі транзакції</option>
                    <option value="SUCCESS">SUCCESS</option>
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
                            <th>ID Trans</th>
                            <th>ID Booking</th>
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
                                <td>
                                    <span className={`${styles.statusBadge} ${styles[p.status?.toLowerCase() || 'pending']}`}>
                                        {p.status}
                                    </span>
                                </td>
                                <td>
                                    {p.status === 'PENDING' ? (
                                        <button onClick={() => handleMarkAsPaid(p)} className={styles.actionBtn}>
                                            Mark as Paid
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
