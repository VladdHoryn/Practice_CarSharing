import React, { useState, useEffect } from 'react';
import { carService } from '../../services/car.service';
import { toast } from 'react-toastify';
import styles from './CarModeration.module.css';

const CarModeration = () => {
    const [queue, setQueue] = useState([]);
    const [loading, setLoading] = useState(true);

    // Стейты для Lightbox (Перегляд фото авто та техпаспортів)
    const [activeImage, setActiveImage] = useState(null);
    const [zoom, setZoom] = useState(1);
    const [rotation, setRotation] = useState(0);

    // Стейты для Модалки відхилення заявки
    const [isRejectModalOpen, setIsRejectModalOpen] = useState(false);
    const [selectedCar, setSelectedCar] = useState(null);
    const [rejectComment, setRejectComment] = useState('');

    const fetchModerationQueue = async () => {
        try {
            setLoading(true);
            // На майбутнє: const data = await carService.getUnconfirmedCars();
            // setQueue(data);
            throw new Error("API method for moderation queue not ready");
        } catch (err) {
            // Мокові дані для автономного тестування інтерфейсу модерації авто
            setQueue([
                {
                    id: 45,
                    brand: 'BMW',
                    model: '320i',
                    year: 2020,
                    vin: 'WBA8K1C01LKKXXXXX',
                    pricePerDay: 45,
                    ownerEmail: 'partner.rent@gmail.com',
                    status: 'UNCONFIRMED',
                    carPhotos: [
                        'https://images.unsplash.com/photo-1555215695-3004980ad54e?q=80&w=600',
                        'https://images.unsplash.com/photo-1525609004556-c46c7d6cf0a3?q=80&w=600'
                    ],
                    techPassportUrl: 'https://images.unsplash.com/photo-1580674684081-7617fbf3d745?q=80&w=600',
                    insuranceUrl: 'https://images.unsplash.com/photo-1450133064473-71024230f91b?q=80&w=600'
                }
            ]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchModerationQueue();
    }, []);

    const handleApproveCar = async (carId) => {
        try {
            // На майбутнє: await carService.approveCar(carId);
            toast.success('Автомобіль успішно прийнято в автопарк! Тепер він доступний в загальному каталозі. 🚗✨');
            setQueue(prev => prev.filter(car => car.id !== carId));
        } catch (err) {
            toast.error('Не вдалося затвердити автомобіль.');
        }
    };

    const openRejectModal = (car) => {
        setSelectedCar(car);
        setRejectComment('');
        setIsRejectModalOpen(true);
    };

    const handleRejectSubmit = async (e) => {
        e.preventDefault();
        if (!rejectComment.trim()) {
            toast.warning('Коментар із причиною відхилення обовʼязковий!');
            return;
        }

        try {
            // На майбутнє: await carService.rejectCar(selectedCar.id, rejectComment);
            toast.info(`Заявку на додавання авто ${selectedCar.brand} відхилено. Менеджеру надіслано коментар.`);
            setIsRejectModalOpen(false);
            setQueue(prev => prev.filter(car => car.id !== selectedCar.id));
        } catch (err) {
            toast.error('Помилка при відхиленні заявки.');
        }
    };

    // Керування Lightbox
    const openLightbox = (url) => {
        setActiveImage(url);
        setZoom(1);
        setRotation(0);
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

                            {/* Лівий блок: Текстові характеристики та фото */}
                            <div className={styles.detailsBlock}>
                                <div className={styles.carMainInfo}>
                                    <h2>{car.brand} {car.model} ({car.year})</h2>
                                    <p className={styles.vinText}>📋 VIN: <code>{car.vin}</code></p>
                                    <p className={styles.metaText}>Власник: {car.ownerEmail} | Тариф: <strong>{car.pricePerDay}€ / доба</strong></p>
                                </div>

                                <div className={styles.gallerySection}>
                                    <h4>📸 Галерея автомобіля:</h4>
                                    <div className={styles.photosGrid}>
                                        {car.carPhotos.map((photo, index) => (
                                            <div key={index} className={styles.photoThumb} onClick={() => openLightbox(photo)}>
                                                <img src={photo} alt={`Фото авто ${index + 1}`} />
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            </div>

                            {/* Правий блок: Перевірка офіційних документів */}
                            <div className={styles.docsBlock}>
                                <h4>📄 Реєстраційні документи:</h4>
                                <div className={styles.docsContainer}>
                                    <div className={styles.docRowCard} onClick={() => openLightbox(car.techPassportUrl)}>
                                        <div className={styles.docIcon}>🪪</div>
                                        <div>
                                            <strong>Технічний паспорт ТЗ</strong>
                                            <small>Клікніть для перевірки даних та VIN</small>
                                        </div>
                                    </div>

                                    <div className={styles.docRowCard} onClick={() => openLightbox(car.insuranceUrl)}>
                                        <div className={styles.docIcon}>📜</div>
                                        <div>
                                            <strong>Договір страхування (Поліс)</strong>
                                            <small>Перевірка терміну дії та франшизи</small>
                                        </div>
                                    </div>
                                </div>

                                <div className={styles.actionButtons}>
                                    <button onClick={() => openRejectModal(car)} className={styles.rejectBtn}>Відхилити</button>
                                    <button onClick={() => handleApproveCar(car.id)} className={styles.approveBtn}>Прийняти в автопарк</button>
                                </div>
                            </div>

                        </div>
                    ))}
                </div>
            )}

            {/* 🖼 INTERACTIVE LIGHTBOX */}
            {activeImage && (
                <div className={styles.lightboxOverlay} onClick={() => setActiveImage(null)}>
                    <div className={styles.lightboxContainer} onClick={(e) => e.stopPropagation()}>
                        <div className={styles.toolbar}>
                            <button onClick={() => setZoom(prev => Math.min(prev + 0.3, 3))}>➕ Збільшити</button>
                            <button onClick={() => setZoom(prev => Math.max(prev - 0.3, 0.6))}>➖ Зменшити</button>
                            <button onClick={() => setRotation(prev => prev + 90)}>🔄 Повернути на 90°</button>
                            <button onClick={() => setActiveImage(null)} className={styles.closeToolbarBtn}>Закрити ✖</button>
                        </div>
                        <div className={styles.imageWrapper}>
                            <img
                                src={activeImage}
                                alt="Документ автомобіля"
                                style={{ transform: `scale(${zoom}) rotate(${rotation}deg)` }}
                            />
                        </div>
                    </div>
                </div>
            )}

            {/* 📝 МОДАЛКА ВІДХИЛЕННЯ ЗАЯВКИ */}
            {isRejectModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <h3>❌ Відхилення публікації ТЗ</h3>
                        <p>Вкажіть коментар для менеджера або власника щодо відмови у додаванні <strong>{selectedCar?.brand} {selectedCar?.model}</strong>:</p>

                        <form onSubmit={handleRejectSubmit} className={styles.rejectForm}>
                            <textarea
                                required
                                rows="4"
                                placeholder="Наприклад: Некоректно вказано VIN-код (невідповідність із техпаспортом) або завантажені фотографії низької якості..."
                                className={styles.textarea}
                                value={rejectComment}
                                onChange={(e) => setRejectComment(e.target.value)}
                            />
                            <div className={styles.modalActions}>
                                <button type="button" onClick={() => setIsRejectModalOpen(false)} className={styles.cancelBtn}>Скасувати</button>
                                <button type="submit" className={styles.confirmRejectBtn}>Надіслати відмову</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CarModeration;
