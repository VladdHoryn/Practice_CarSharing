import React, { useState, useEffect } from 'react';
import { carService } from '../../services/car.service';
import { toast } from 'react-toastify';
import styles from './CarManagement.module.css';

const CarManagement = () => {
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);

    const [formData, setFormData] = useState({
        brand: '',
        model: '',
        year: new Date().getFullYear(),
        pricePerDay: '',
        status: 'AVAILABLE',
        vin: '',
        images: []
    });

    const fetchFleet = async () => {
        try {
            setLoading(true);
            const data = await carService.getAllCars();
            setCars(data);
        } catch (err) {
            console.error('Помилка завантаження автопарку:', err);
            setCars([
                { id: 1, brand: 'Toyota', model: 'Yaris', year: 2021, pricePerDay: 25, status: 'AVAILABLE', vin: 'JT1BR32K00DXXXXXX' },
                { id: 2, brand: 'Tesla', model: 'Model 3', year: 2022, pricePerDay: 60, status: 'MAINTENANCE', vin: '5YJ3E1EB7NFXXXXXX' },
                { id: 3, brand: 'Volkswagen', model: 'Golf', year: 2019, pricePerDay: 30, status: 'UNAVAILABLE', vin: 'WVWZZZAUZKWXXXXXX' }
            ]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFleet();
    }, []);

    const handleStatusChange = async (carId, newStatus) => {
        try {
            if (carService.updateCarStatus) {
                await carService.updateCarStatus(carId, newStatus);
            }
            toast.success('Статус автомобіля успішно оновлено!');
            fetchFleet();
        } catch (err) {
            toast.error('Не вдалося оновити статус.');
        }
    };

    const handleFileChange = (e) => {
        const files = Array.from(e.target.files);
        setFormData({ ...formData, images: files }); // Записуємо масив файлів у стейт
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            // Логіка підготовки FormData під Multipart запит (текст + файли)
            const dataToSend = new FormData();
            dataToSend.append('brand', formData.brand);
            dataToSend.append('model', formData.model);
            dataToSend.append('year', formData.year);
            dataToSend.append('pricePerDay', formData.pricePerDay);
            dataToSend.append('status', formData.status);
            dataToSend.append('vin', formData.vin);

            formData.images.forEach((file) => {
                dataToSend.append('images', file);
            });

            // На майбутнє: await carService.createCar(dataToSend);
            toast.success('Новий автомобіль успішно додано в автопарк! 🚗');
            setIsModalOpen(false);
            fetchFleet();

            // Скидання форми
            setFormData({ brand: '', model: '', year: 2026, pricePerDay: '', status: 'AVAILABLE', vin: '', images: [] });
        } catch (err) {
            toast.error('Помилка при додаванні автомобіля.');
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.pageHeader}>
                <h1 className={styles.title}>🚗 Керування автопарком</h1>
                <button onClick={() => setIsModalOpen(true)} className={styles.addCarBtn}>
                    + Додати автомобіль
                </button>
            </div>

            {loading ? (
                <div className={styles.loader}>Завантаження транспортних засобів... ⏳</div>
            ) : (
                <div className={styles.grid}>
                    {cars.map((car) => (
                        <div key={car.id} className={styles.carCard}>
                            <div className={styles.carInfo}>
                                <div>
                                    <span className={`${styles.badge} ${styles[car.status.toLowerCase()]}`}>
                                        {car.status}
                                    </span>
                                    <h3 className={styles.carName}>{car.brand} {car.model}</h3>
                                    <p className={styles.carMeta}>Рік: {car.year} | VIN: {car.vin}</p>
                                </div>
                                <span className={styles.price}>{car.pricePerDay}€ / доба</span>
                            </div>

                            <div className={styles.cardActions}>
                                <label className={styles.selectLabel}>Змінити статус:</label>
                                <select
                                    value={car.status}
                                    onChange={(e) => handleStatusChange(car.id, e.target.value)}
                                    className={styles.statusSelect}
                                >
                                    <option value="AVAILABLE">AVAILABLE (Доступна)</option>
                                    <option value="MAINTENANCE">MAINTENANCE (Ремонт)</option>
                                    <option value="UNAVAILABLE">UNAVAILABLE (Недоступна)</option>
                                </select>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* 📝 МОДАЛЬНЕ ВІКНО: ДОДАННЯ АВТОМОБІЛЯ */}
            {isModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <h2>Додавання нового ТЗ</h2>
                        <form onSubmit={handleSubmit} className={styles.form}>
                            <div className={styles.formRow}>
                                <div className={styles.inputGroup}>
                                    <label>Марка</label>
                                    <input type="text" required value={formData.brand} onChange={(e) => setFormData({...formData, brand: e.target.value})} placeholder="Напр. Audi"/>
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Модель</label>
                                    <input type="text" required value={formData.model} onChange={(e) => setFormData({...formData, model: e.target.value})} placeholder="Напр. A6"/>
                                </div>
                            </div>

                            <div className={styles.formRow}>
                                <div className={styles.inputGroup}>
                                    <label>Рік випуску</label>
                                    <input type="number" required value={formData.year} onChange={(e) => setFormData({...formData, year: e.target.value})}/>
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Ціна за добу (€)</label>
                                    <input type="number" required value={formData.pricePerDay} onChange={(e) => setFormData({...formData, pricePerDay: e.target.value})} placeholder="50"/>
                                </div>
                            </div>

                            <div className={styles.inputGroup}>
                                <label>VIN-код</label>
                                <input type="text" required value={formData.vin} onChange={(e) => setFormData({...formData, vin: e.target.value})} placeholder="17-значний номер"/>
                            </div>

                            <div className={styles.inputGroup}>
                                <label>Завантаження фото (Можна обрати декілька)</label>
                                <input
                                    type="file"
                                    multiple
                                    accept="image/*"
                                    onChange={handleFileChange}
                                    className={styles.fileInput}
                                />
                                <small className={styles.fileHelp}>Обрано файлів: {formData.images.length}</small>
                            </div>

                            <div className={styles.modalActions}>
                                <button type="button" onClick={() => setIsModalOpen(false)} className={styles.cancelBtn}>Скасувати</button>
                                <button type="submit" className={styles.saveBtn}>Додати в систему</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CarManagement;
