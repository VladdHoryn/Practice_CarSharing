import React, { useState, useEffect } from 'react';
import { carService } from '../../services/car.service';
import { toast } from 'react-toastify';
import SecureImage from '../../components/SecureImage';
import styles from './CarManagement.module.css';

const CarManagement = () => {
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingCarId, setEditingCarId] = useState(null);
    const [selectedFiles, setSelectedFiles] = useState([]);
    const [carToDelete, setCarToDelete] = useState(null);

    // 👑 ДОДАНО: Стейт для перегляду розширеної галереї фотографій авто
    const [viewingCar, setViewingCar] = useState(null);
    const [carGallery, setCarGallery] = useState([]);
    const [galleryLoading, setGalleryLoading] = useState(false);

    const [classFilter, setClassFilter] = useState('ALL');
    const [searchBrand, setSearchBrand] = useState('');
    const [searchModel, setSearchModel] = useState('');
    const [searchYear, setSearchYear] = useState('');
    const [maxPrice, setMaxPrice] = useState(200);
    const [sortBy, setSortBy] = useState('newest');

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

    // 👑 ДОДАНО: Метод підвантаження всіх пов'язаних зображень для обраного ТЗ
    const handleOpenGallery = async (car) => {
        try {
            setViewingCar(car);
            setGalleryLoading(true);
            setCarGallery([]);
            const images = await carService.getCarImages(car.id);
            setCarGallery(images || []);
        } catch (err) {
            console.error("Не вдалося отримати галерею автопарку:", err);
            toast.error("Помилка завантаження фотографій.");
        } finally {
            setGalleryLoading(false);
        }
    };

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

    const confirmDeleteCar = async () => {
        if (!carToDelete) return;
        try {
            await carService.deleteCar(carToDelete);
            toast.success('Automobile successfully deleted from storage.');
            setCarToDelete(null);
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
        setSelectedFiles([]);
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

            let carId = editingCarId;

            if (editingCarId) {
                await carService.updateCar(editingCarId, payload);
                toast.success('Дані автомобіля успішно оновлено! ✏️');
            } else {
                const newCar = await carService.createCar(payload);
                carId = newCar.id;
                toast.success('Новий автомобіль успішно додано в автопарк! 🚗');
            }

            if (selectedFiles.length > 0 && carId) {
                for (const file of selectedFiles) {
                    await carService.uploadCarImage(carId, file);
                }
            }

            setIsModalOpen(false);
            setEditingCarId(null);
            setSelectedFiles([]);
            fetchFleet();
            setFormData({ brand: '', model: '', year: 2026, pricePerDay: '', carClass: 'BUSINESS', imageUrl: '', userId: 2 });
        } catch (err) {
            toast.error('Помилка при збереженні автомобіля.');
        }
    };

    const filteredCars = cars.filter(car => {
        const matchesClass = classFilter === 'ALL' || car.carClass === classFilter;
        const matchesBrand = !searchBrand || car.brand?.toLowerCase().includes(searchBrand.toLowerCase().trim());
        const matchesModel = !searchModel || car.model?.toLowerCase().includes(searchModel.toLowerCase().trim());
        const matchesYear = !searchYear || car.year?.toString() === searchYear.trim();
        const matchesPrice = car.pricePerDay <= maxPrice;
        return matchesClass && matchesBrand && matchesModel && matchesYear && matchesPrice;
    }).sort((a, b) => {
        if (sortBy === 'price-asc') return a.pricePerDay - b.pricePerDay;
        if (sortBy === 'price-desc') return b.pricePerDay - a.pricePerDay;
        if (sortBy === 'year-desc') return b.year - a.year;
        if (sortBy === 'newest') return b.id - a.id;
        return 0;
    });

    return (
        <div className={styles.container}>
            <div className={styles.pageHeader}>
                <h1 className={styles.title}>🚗 Керування автопарком</h1>
                <button onClick={() => { setEditingCarId(null); setSelectedFiles([]); setIsModalOpen(true); }} className={styles.addCarBtn}>
                    + Додати автомобіль
                </button>
            </div>

            <div style={{ marginBottom: '20px', display: 'grid', gridTemplateColumns: 'repeat(3, 1fr) 140px 140px', gap: '12px', background: '#fff', padding: '15px', borderRadius: '8px', border: '1px solid #eef0f2' }}>
                <input type="text" placeholder="Марка..." value={searchBrand} onChange={e => setSearchBrand(e.target.value)} style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', fontSize: '13px' }} />
                <input type="text" placeholder="Модель..." value={searchModel} onChange={e => setSearchModel(e.target.value)} style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', fontSize: '13px' }} />
                <input type="number" placeholder="Рік..." value={searchYear} onChange={e => setSearchYear(e.target.value)} style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', fontSize: '13px' }} />

                <select value={classFilter} onChange={e => setClassFilter(e.target.value)} style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', fontSize: '13px' }}>
                    <option value="ALL">Всі класи</option>
                    <option value="ECONOMY">ECONOMY</option>
                    <option value="COMFORT">COMFORT</option>
                    <option value="BUSINESS">BUSINESS</option>
                    <option value="LUXURY">LUXURY</option>
                </select>

                <select value={sortBy} onChange={e => setSortBy(e.target.value)} style={{ padding: '8px', borderRadius: '4px', border: '1px solid #ccc', fontSize: '13px' }}>
                    <option value="newest">Нові оголошення</option>
                    <option value="price-asc">Ціна: зростання</option>
                    <option value="price-desc">Ціна: спадання</option>
                    <option value="year-desc">Рок: спочатку нові</option>
                </select>

                <div style={{ gridColumn: '1 / span 5', display: 'flex', alignItems: 'center', gap: '10px', marginTop: '5px' }}>
                    <span style={{ fontSize: '13px', color: '#555', minWidth: '120px' }}>Ціна до: <strong>{maxPrice}€</strong></span>
                    <input type="range" min="10" max="200" step="5" value={maxPrice} onChange={e => setMaxPrice(Number(e.target.value))} style={{ flex: 1 }} />
                </div>
            </div>

            {loading ? (
                <div className={styles.loader}>Завантаження транспортних засобів... ⏳</div>
            ) : (
                <div className={styles.grid}>
                    {filteredCars.map((car) => (
                        <div key={car.id} className={styles.carCard}>
                            {/* 👑 ФІКС: Зробили головне прев'ю клікабельним для перегляду галереї */}
                            <div
                                onClick={() => handleOpenGallery(car)}
                                style={{ height: '140px', background: '#f5f5f5', borderRadius: '6px 6px 0 0', overflow: 'hidden', marginBottom: '10px', cursor: 'pointer', position: 'relative' }}
                                title="Відкрити медіа-галерею авто"
                            >
                                <SecureImage src={`/car/v1/${car.id}/images/main`} alt={car.brand} style={{ width: '100%', height: '100%' }} />
                                <div style={{ position: 'absolute', top: '6px', right: '6px', background: 'rgba(0,0,0,0.6)', color: '#fff', padding: '2px 6px', borderRadius: '4px', fontSize: '10px', fontWeight: 'bold' }}>
                                    👁️ Галерея
                                </div>
                            </div>

                            <div className={styles.carInfo} style={{ padding: '0 10px' }}>
                                <div>
                                    <span className={`${styles.badge} ${styles[car.status?.toLowerCase() || 'available']}`}>
                                        {car.status}
                                    </span>
                                    <h3 className={styles.carName} style={{ marginTop: '5px' }}>{car.brand} {car.model}</h3>
                                    <p className={styles.carMeta}>Рік: {car.year} | Клас: {car.carClass}</p>
                                </div>
                                <span className={styles.price}>{car.pricePerDay}€ / доба</span>
                            </div>

                            <div className={styles.cardActions} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '10px' }}>
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

                                <div style={{ display: 'flex', gap: '10px' }}>
                                    {/* 👑 ДОДАНО: Кнопка швидкого виклику галереї */}
                                    <button onClick={() => handleOpenGallery(car)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '18px' }} title="Переглянути фото">👁️</button>
                                    <button onClick={() => openEditModal(car)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '18px' }} title="Редагувати">✏️</button>
                                    <button onClick={() => setCarToDelete(car.id)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '18px' }} title="Видалити">🗑️</button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Модалка створення/редагування авто */}
            {isModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal} style={{ maxWidth: '520px' }}>
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
                            <div className={styles.inputGroup} style={{ marginBottom: '15px' }}>
                                <label>Клас авто</label>
                                <select value={formData.carClass} onChange={(e) => setFormData({...formData, carClass: e.target.value})} className={styles.statusSelect} style={{ width: '100%' }}>
                                    <option value="ECONOMY">ECONOMY</option>
                                    <option value="COMFORT">COMFORT</option>
                                    <option value="BUSINESS">BUSINESS</option>
                                    <option value="LUXURY">LUXURY</option>
                                </select>
                            </div>

                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ display: 'block', marginBottom: '6px', fontWeight: 'bold', fontSize: '13px' }}>📸 Пакет медіафайлів автомобіля</label>
                                <input
                                    type="file"
                                    multiple
                                    accept="image/png, image/jpeg, image/jpg"
                                    onChange={(e) => {
                                        if (e.target.files) {
                                            const filesArray = Array.from(e.target.files);
                                            setSelectedFiles(prevFiles => {
                                                const uniqueNewFiles = filesArray.filter(
                                                    newFile => !prevFiles.some(prevFile => prevFile.name === newFile.name && prevFile.size === newFile.size)
                                                );
                                                return [...prevFiles, ...uniqueNewFiles];
                                            });
                                        }
                                    }}
                                    style={{ width: '100%', padding: '6px', border: '1px dashed #0056b3', borderRadius: '4px', background: '#f8fbff', cursor: 'pointer' }}
                                />

                                {selectedFiles.length > 0 && (
                                    <div style={{ marginTop: '10px', maxHeight: '110px', overflowY: 'auto', background: '#f9f9f9', padding: '8px', borderRadius: '4px', border: '1px solid #ddd' }}>
                                        <div style={{ fontSize: '11px', fontWeight: 'bold', marginBottom: '4px' }}>Готові до підвантаження ({selectedFiles.length}):</div>
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                                            {selectedFiles.map((file, i) => (
                                                <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#fff', padding: '3px 6px', borderRadius: '4px', fontSize: '12px', border: '1px solid #eee' }}>
                                                    <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '85%' }}>📄 {file.name}</span>
                                                    <button type="button" onClick={() => setSelectedFiles(selectedFiles.filter((_, idx) => idx !== i))} style={{ background: 'none', border: 'none', color: '#dc3545', fontWeight: 'bold', cursor: 'pointer' }}>✕</button>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div className={styles.modalActions}>
                                <button type="button" onClick={() => setIsModalOpen(false)} className={styles.cancelBtn}>Скасувати</button>
                                <button type="submit" className={styles.saveBtn}>Зберегти</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* 👑 ДОДАНО: Модальне вікно перегляду ПОВНОЇ фотогалереї для автопарку (як у модерації) */}
            {viewingCar && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1500 }}>
                    <div style={{ background: '#fff', padding: '25px', borderRadius: '12px', width: '600px', maxWidth: '90%', maxHeight: '85vh', overflowY: 'auto' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                            <h3 style={{ margin: 0 }}>Галерея медіафайлів {viewingCar.brand} {viewingCar.model}</h3>
                            <button onClick={() => setViewingCar(null)} style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer' }}>✕</button>
                        </div>

                        <p style={{ fontSize: '14px', margin: '0 0 15px 0', color: '#555' }}>
                            Клас: <strong>{viewingCar.carClass}</strong> | Рік: {viewingCar.year} | Тариф: <strong>{viewingCar.pricePerDay}€ / доба</strong>
                        </p>

                        <h4>🖼️ Завантажені фотографії ({carGallery.length}):</h4>
                        {galleryLoading ? (
                            <div style={{ color: '#666', fontStyle: 'italic', padding: '20px 0', textAlign: 'center' }}>Завантаження зображень з системи... ⏳</div>
                        ) : carGallery.length > 0 ? (
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginBottom: '10px' }}>
                                {carGallery.map(img => (
                                    <div key={img.id} style={{ height: '110px', borderRadius: '6px', overflow: 'hidden', background: '#eee', border: img.isMain ? '2px solid #28a745' : '1px solid #ddd', position: 'relative' }}>
                                        <SecureImage src={`/car/v1/${viewingCar.id}/images/${img.id}`} alt="Fleet piece" style={{ width: '100%', height: '100%' }} />
                                        {img.isMain && <span style={{ position: 'absolute', bottom: '4px', left: '4px', background: '#28a745', color: '#fff', fontSize: '10px', padding: '2px 4px', borderRadius: '3px', fontWeight: 'bold' }}>MAIN</span>}
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <p style={{ color: '#888', fontStyle: 'italic', fontSize: '13px', textAlign: 'center', padding: '15px' }}>Фотографії для цього транспортного засобу відсутні.</p>
                        )}

                        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', borderTop: '1px solid #eee', paddingTop: '15px', marginTop: '15px' }}>
                            <button onClick={() => { setViewingCar(null); openEditModal(viewingCar); }} style={{ padding: '8px 16px', background: '#0056b3', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Редагувати авто ✏️</button>
                            <button onClick={() => setViewingCar(null)} style={{ padding: '8px 16px', background: '#e2e8f0', color: '#4a5568', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Закрити</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Кастомний стильній Alert видалення авто */}
            {carToDelete && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000 }}>
                    <div style={{ background: '#fff', padding: '25px', borderRadius: '10px', width: '380px', textAlign: 'center', boxShadow: '0 4px 15px rgba(0,0,0,0.15)' }}>
                        <div style={{ fontSize: '40px', marginBottom: '10px' }}>🗑️</div>
                        <h3 style={{ margin: '0 0 10px 0', color: '#1a1a1a' }}>Видалення автомобіля</h3>
                        <p style={{ fontSize: '14px', color: '#666', margin: '0 0 20px 0', lineHeight: '1.4' }}>
                            Ви впевнені, що хочете незворотно видалити авто з бази даних? Доступ до активних бронювань буде скасовано.
                        </p>
                        <div style={{ display: 'flex', gap: '12px', justifyContent: 'center' }}>
                            <button onClick={() => setCarToDelete(null)} style={{ padding: '8px 18px', background: '#e2e8f0', color: '#4a5568', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Скасувати</button>
                            <button onClick={confirmDeleteCar} style={{ padding: '8px 18px', background: '#dc3545', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>Видалити</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CarManagement;
