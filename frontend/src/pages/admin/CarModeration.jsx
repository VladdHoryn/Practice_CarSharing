import React, { useState, useEffect } from 'react';
import { carService } from '../../services/car.service';
import { toast } from 'react-toastify';
import styles from './CarModeration.module.css';

const CarModeration = () => {
    const [queue, setQueue] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeImage, setActiveImage] = useState(null);

    const fetchModerationQueue = async () => {
        try {
            setLoading(true);
            // 👑 ВИПРАВЛЕНО: Реальний запит черги модерації непідтверджених авто
            const data = await carService.getUnconfirmedCars();
            setQueue(data || []);
        } catch (err) {
            console.error('Помилка завантаження черги модерації:', err);
            toast.error('Не вдалося завантажити чергу модерації.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchModerationQueue();
    }, []);

    const handleApproveCar = async (carId) => {
        try {
            // 👑 ВИПРАВЛЕНО: Виклик нашого виправленого сервісу confirmModeration
            await carService.confirmModeration(carId);
            toast.success('Автомобіль успішно прийнято в автопарк! 🚗✨');
            setQueue(prev => prev.filter(car => car.id !== carId));
        } catch (err) {
            toast.error('Не вдалося затвердити автомобіль.');
        }
    };

    const handleRejectSubmit = async (carId) => {
        if (!window.confirm("Скасувати публікацію цього ТЗ?")) return;
        try {
            // 👑 ВИПРАВЛЕНО: Виклик нашого виправленого сервісу cancelModeration
            await carService.cancelModeration(carId);
            toast.info(`Заявку на додавання авто відхилено.`);
            setQueue(prev => prev.filter(car => car.id !== carId));
        } catch (err) {
            toast.error('Помилка при відхиленні заявки.');
        }
    };

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>🔍 Модерація нових транспортних засобів</h1>

            {loading ? (
                <div className={styles.loader}>Завантаження заявок на публікацію... ⏳</div>
            ) : queue.length === 0 ? (
                <div className={styles.emptyState}>Немає нових заявок на модерацію авто. Парк перевірено! ✅</div>
            ) : (
                <div className={styles.queueList}>
                    {queue.map((car) => (
                        <div key={car.id} className={styles.moderationCard}>
                            <div className={styles.detailsBlock}>
                                <div className={styles.carMainInfo}>
                                    <h2>{car.brand} {car.model} ({car.year})</h2>
                                    <p className={styles.metaText}>Власник (ID): {car.userId} | Тариф: <strong>{car.pricePerDay}€ / доба</strong></p>
                                    <p className={styles.metaText}>Клас: {car.carClass} | Статус: {car.status}</p>
                                </div>
                                {car.imageUrl && (
                                    <div className={styles.photoThumb} onClick={() => setActiveImage(car.imageUrl)} style={{ width: '150px', cursor: 'pointer', marginTop: '10px' }}>
                                        <img src={car.imageUrl} alt="Car" style={{ width: '100%', borderRadius: '4px' }} />
                                    </div>
                                )}
                            </div>
                            <div style={{ display: 'flex', gap: '15px', marginTop: '15px' }}>
                                <button onClick={() => handleRejectSubmit(car.id)} className={styles.rejectBtn}>Відхилити ✖</button>
                                <button onClick={() => handleApproveCar(car.id)} className={styles.approveBtn}>Прийняти в автопарк 👍</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default CarModeration;
