import React, { useState, useEffect } from 'react';
import { carService } from '../../services/car.service';
import { toast } from 'react-toastify';
import styles from './CarManagement.module.css';

const CarManagement = () => {
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingCarId, setEditingCarId] = useState(null);

    // 👑 ДОДАНО: Локальні фільтри
    const [classFilter, setClassFilter] = useState('ALL');
    const [searchBrand, setSearchBrand] = useState('');

    const [formData, setFormData] = useState({
        brand: '', model: '', year: 2026, pricePerDay: '', carClass: 'BUSINESS', imageUrl: '', userId: 2
    });

    const fetchFleet = async () => {
        try {
            setLoading(true);
            const data = await carService.getAllCars();
            setCars(data || []);
        } catch (err) {
            console.error('Помилка завантаження автопарку:', err);
            toast.error('Не вдалося завантажити транспортні засоби.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchFleet();
    }, []);

    const handleStatusChange = async (carId, newStatus) => {
        try {
            await carService.changeCarStatus(carId, newStatus);

            toast.success('Статус автомобіля успішно оновлено!');
            fetchFleet();
        } catch (err) {
            console.error("Помилка зміни статусу авто:", err);
            toast.error('Не вдалося оновити статус.');
        }
    };

    const handleDeleteCar = async (carId) => {
        if (!window.confirm("🗑️ Ви впевнені, що хочете видалити це авто з системи?")) return;
        try {
            await carService.deleteCar(carId);
            toast.success('Автомобіль успішно видалено з бази даних!');
            fetchFleet();
        } catch (err) {
            toast.error('Помилка при видаленні автомобіля.');
        }
    };

    const openEditModal = (car) => {
        setEditingCarId(car.id);
        setFormData({
            brand: car.brand,
            model: car.model,
            year: car.year,
            pricePerDay: car.pricePerDay,
            carClass: car.carClass || 'BUSINESS',
            imageUrl: car.imageUrl || '',
            userId: car.userId || 2
        });
        setIsModalOpen(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                ...formData,
                year: Number(formData.year),
                pricePerDay: Number(formData.pricePerDay),
                userId: Number(formData.userId),
                carClass: formData.carClass.toUpperCase()
            };

            if (editingCarId) {
                // 👑 ДОДАНО: Реальний PUT запит оновлення
                await carService.updateCar(editingCarId, payload);
                toast.success('Дані автомобіля успішно оновлено! ✏️');
            } else {
                // 👑 ВИПРАВЛЕНО: Реальний POST запит під ТЗ без Multipart
                await carService.createCar(payload);
                toast.success('Новий автомобіль успішно додано в автопарк! 🚗');
            }

            setIsModalOpen(false);
            setEditingCarId(null);
            fetchFleet();
            setFormData({ brand: '', model: '', year: 2026, pricePerDay: '', carClass: 'BUSINESS', imageUrl: '', userId: 2 });
        } catch (err) {
            toast.error('Помилка при збереженні автомобіля.');
        }
    };

    // 👑 ДОДАНО: Локальна фільтрація
    const filteredCars = cars.filter(car => {
        const matchesClass = classFilter === 'ALL' || car.carClass === classFilter;
        const matchesBrand = car.brand?.toLowerCase().includes(searchBrand.toLowerCase());
        return matchesClass && matchesBrand;
    });

    return (
        <div className={styles.container}>
            <div className={styles.pageHeader}>
                <h1 className={styles.title}>🚗 Керування автопарком</h1>
                <button onClick={() => { setEditingCarId(null); setIsModalOpen(true); }} className={styles.addCarBtn}>
                    + Додати автомобіль
                </button>
            </div>

            {/* 👑 Фільтр-панель */}
            <div style={{ marginBottom: '15px', display: 'flex', gap: '15px' }}>
                <input type="text" placeholder="Пошук за маркою..." value={searchBrand} onChange={e => setSearchBrand(e.target.value)} style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc' }} />
                <select value={classFilter} onChange={e => setClassFilter(e.target.value)} style={{ padding: '8px', borderRadius: '4px' }}>
                    <option value="ALL">Всі класи</option>
                    <option value="BUSINESS">BUSINESS</option>
                    <option value="ECONOMY">ECONOMY</option>
                    <option value="COMFORT">COMFORT</option>
                    <option value="LUXURY">LUXURY</option>
                </select>
            </div>

            {loading ? (
                <div className={styles.loader}>Завантаження транспортних засобів... ⏳</div>
            ) : (
                <div className={styles.grid}>
                    {filteredCars.map((car) => (
                        <div key={car.id} className={styles.carCard}>
                            <div className={styles.carInfo}>
                                <div>
                                    <span className={`${styles.badge} ${styles[car.status?.toLowerCase() || 'available']}`}>
                                        {car.status}
                                    </span>
                                    <h3 className={styles.carName}>{car.brand} {car.model}</h3>
                                    <p className={styles.carMeta}>Рік: {car.year} | Клас: {car.carClass}</p>
                                </div>
                                <span className={styles.price}>{car.pricePerDay}€ / доба</span>
                            </div>

                            <div className={styles.cardActions} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <select
                                    value={car.status}
                                    onChange={(e) => handleStatusChange(car.id, e.target.value)}
                                    className={styles.statusSelect}
                                >
                                    <option value="AVAILABLE">AVAILABLE</option>
                                    <option value="MAINTENANCE">MAINTENANCE</option>
                                    <option value="UNAVAILABLE">UNAVAILABLE</option>
                                    <option value="UNCONFIRMED">UNCONFIRMED</option>
                                </select>

                                {/* 👑 ДОДАНО: Смітник та Олівчик */}
                                <div style={{ display: 'flex', gap: '10px' }}>
                                    <button onClick={() => openEditModal(car)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '18px' }} title="Редагувати">✏️</button>
                                    <button onClick={() => handleDeleteCar(car.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '18px' }} title="Видалити">🗑️</button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Модалка створення/редагування ТЗ */}
            {isModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <h2>{editingCarId ? '✏️ Редагування ТЗ' : '🚗 Додавання нового ТЗ'}</h2>
                        <form onSubmit={handleSubmit} className={styles.form}>
                            <div className={styles.formRow}>
                                <div className={styles.inputGroup}><label>Марка</label><input type="text" required value={formData.brand} onChange={(e) => setFormData({...formData, brand: e.target.value})}/></div>
                                <div className={styles.inputGroup}><label>Модель</label><input type="text" required value={formData.model} onChange={(e) => setFormData({...formData, model: e.target.value})}/></div>
                            </div>
                            <div className={styles.formRow}>
                                <div className={styles.inputGroup}><label>Рік випуску</label><input type="number" required value={formData.year} onChange={(e) => setFormData({...formData, year: e.target.value})}/></div>
                                <div className={styles.inputGroup}><label>Ціна за добу (€)</label><input type="number" step="0.1" required value={formData.pricePerDay} onChange={(e) => setFormData({...formData, pricePerDay: e.target.value})}/></div>
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Клас авто</label>
                                <select value={formData.carClass} onChange={(e) => setFormData({...formData, carClass: e.target.value})} className={styles.statusSelect} style={{ width: '100%' }}>
                                    <option value="ECONOMY">ECONOMY</option>
                                    <option value="COMFORT">COMFORT</option>
                                    <option value="BUSINESS">BUSINESS</option>
                                    <option value="LUXURY">LUXURY</option>
                                </select>
                            </div>
                            <div className={styles.inputGroup}><label>URL фотографії</label><input type="text" value={formData.imageUrl} onChange={(e) => setFormData({...formData, imageUrl: e.target.value})} placeholder="https://..."/></div>
                            <div className={styles.modalActions}>
                                <button type="button" onClick={() => setIsModalOpen(false)} className={styles.cancelBtn}>Скасувати</button>
                                <button type="submit" className={styles.saveBtn}>Зберегти</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CarManagement;
