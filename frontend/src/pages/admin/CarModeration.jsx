import React, { useState, useEffect } from 'react';
import { carService } from '../../services/car.service';
import { toast } from 'react-toastify';
import SecureImage from '../../components/SecureImage';
import styles from './CarModeration.module.css';

const CarModeration = () => {
    const [queue, setQueue] = useState([]);
    const [loading, setLoading] = useState(true);

    const [viewingCar, setViewingCar] = useState(null);
    const [carGallery, setCarGallery] = useState([]);
    const [galleryLoading, setImagesLoading] = useState(false);

    const fetchModerationQueue = async () => {
        try {
            setLoading(true);
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

    const handleOpenGallery = async (car) => {
        try {
            setViewingCar(car);
            setImagesLoading(true);
            setCarGallery([]);
            const images = await carService.getCarImages(car.id);
            setCarGallery(images || []);
        } catch (err) {
            console.error("Не вдалося завантажити галерею авто:", err);
            toast.error("Помилка завантаження фотографій.");
        } finally {
            setImagesLoading(false);
        }
    };

    const handleApproveCar = async (carId) => {
        try {
            await carService.confirmModeration(carId);
            toast.success('Автомобіль успішно прийнято в автопарк! 🚗✨');
            setQueue(prev => prev.filter(car => car.id !== carId));
            setViewingCar(null);
        } catch (err) {
            toast.error('Не вдалося затвердити автомобіль.');
        }
    };

    const handleRejectSubmit = async (carId) => {
        if (!window.confirm("Скасувати публікацію цього ТЗ?")) return;
        try {
            await carService.cancelModeration(carId);
            toast.info(`Заявку на додавання авто відхилено.`);
            setQueue(prev => prev.filter(car => car.id !== carId));
            setViewingCar(null);
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

                                {}
                                <div className={styles.photoThumb} onClick={() => handleOpenGallery(car)} style={{ width: '160px', height: '100px', cursor: 'pointer', marginTop: '10px', overflow: 'hidden', borderRadius: '6px', background: '#eaeaea' }} title="Переглянути всі фото">
                                    <SecureImage src={`/car/v1/${car.id}/images/main`} alt="Car Main preview" style={{ width: '100%', height: '100%' }} />
                                </div>
                            </div>
                            <div style={{ display: 'flex', gap: '12px', marginTop: '15px', alignItems: 'center' }}>
                                <button onClick={() => handleRejectSubmit(car.id)} className={styles.rejectBtn}>Відхилити ✖</button>
                                {}
                                <button onClick={() => handleOpenGallery(car)} style={{ padding: '8px 14px', border: '1px solid #ccc', background: '#fff', borderRadius: '4px', cursor: 'pointer', fontSize: '13px', fontWeight: '500' }}>ⓘ Детальніше</button>
                                {}
                                <button onClick={() => handleApproveCar(car.id)} className={styles.approveBtn}>Прийняти в автопарк</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {}
            {viewingCar && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1500 }}>
                    <div style={{ background: '#fff', padding: '25px', borderRadius: '12px', width: '600px', maxWidth: '90%', maxHeight: '85vh', overflowY: 'auto' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                            <h3 style={{ margin: 0 }}>Специфікація {viewingCar.brand} {viewingCar.model}</h3>
                            <button onClick={() => setViewingCar(null)} style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer' }}>✕</button>
                        </div>

                        <p style={{ fontSize: '14px', margin: '0 0 15px 0', color: '#555' }}>
                            Клас: <strong>{viewingCar.carClass}</strong> | Рік: {viewingCar.year} | Власник ID: {viewingCar.userId} | Вартість: {viewingCar.pricePerDay}€/доба
                        </p>

                        <h4>🖼️ Повна медіа-галерея авто ({carGallery.length} фото):</h4>
                        {galleryLoading ? (
                            <div style={{ color: '#666', fontStyle: 'italic', padding: '20px 0' }}>Завантаження фотографій... ⏳</div>
                        ) : carGallery.length > 0 ? (
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginBottom: '20px' }}>
                                {carGallery.map(img => (
                                    <div key={img.id} style={{ height: '110px', borderRadius: '6px', overflow: 'hidden', background: '#eee', border: img.isMain ? '2px solid #28a745' : '1px solid #ddd', position: 'relative' }}>
                                        <SecureImage src={`/car/v1/${viewingCar.id}/images/${img.id}`} alt="Gallery piece" style={{ width: '100%', height: '100%' }} />
                                        {img.isMain && <span style={{ position: 'absolute', bottom: '4px', left: '4px', background: '#28a745', color: '#fff', fontSize: '10px', padding: '2px 4px', borderRadius: '3px', fontWeight: 'bold' }}>MAIN</span>}
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p style={{ color: '#888', fontStyle: 'italic', fontSize: '13px' }}>Додаткових фотографій немає.</p>
                        )}

                        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', borderTop: '1px solid #eee', paddingTop: '15px' }}>
                            <button onClick={() => handleRejectSubmit(viewingCar.id)} style={{ padding: '8px 16px', background: '#dc3545', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Відхилити заявку</button>
                            <button onClick={() => handleApproveCar(viewingCar.id)} style={{ padding: '8px 16px', background: '#28a745', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Підтвердити авто</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CarModeration;
