import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './CarCatalogPage.module.css';
import { carService } from '../services/car.service';
import { bookingService } from '../services/booking.service';
import SecureImage from '../components/SecureImage';
import { toast } from 'react-toastify';

const CarCatalogPage = () => {
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const [availableCarIds, setAvailableCarIds] = useState(null);
    const todayStr = new Date().toLocaleDateString('en-CA');

    const [filters, setFilters] = useState(() => {
         const saved = localStorage.getItem('carFilters');
         return saved ? JSON.parse(saved) : { brand: 'all', maxPrice: 200, startDate: '', endDate: '' };
    });

    useEffect(() => {
        const fetchCars = async () => {
            try {
                setLoading(true);
                const data = await carService.getAvailableCars();
                setCars(data);
                setError(null);
            } catch (err) {
                console.error('Помилка завантаження авто:', err);
                setError('Не вдалося зв\'язатися з сервером.');
            } finally {
                setLoading(false);
            }
        };
        fetchCars();
    }, []);

    useEffect(() => {
        localStorage.setItem('carFilters', JSON.stringify(filters));

        if (!filters.startDate || !filters.endDate) {
            setAvailableCarIds(null);
            return;
        }

        if (new Date(filters.endDate) <= new Date(filters.startDate)) {
            toast.error('Дата завершення оренди має бути більшою за дату початку!');
            return;
        }

        const loadAvailableCarIds = async () => {
            try {
                const formattedStart = `${filters.startDate}T12:00:00`;
                const formattedEnd = `${filters.endDate}T12:00:00`;
                const freeIds = await bookingService.getAvailableCarIds(formattedStart, formattedEnd);
                setAvailableCarIds(freeIds);
            } catch (err) {
                console.error("Помилка фільтрації авто за датами:", err);
                setAvailableCarIds([]);
            }
        };
        loadAvailableCarIds();
    }, [filters]);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => {
            if (name === 'startDate' && prev.endDate && new Date(value) >= new Date(prev.endDate)) {
                return { ...prev, startDate: value, endDate: '' };
            }
            return { ...prev, [name]: value };
        });
    };

    const resetFilters = () => {
        setFilters({ brand: 'all', maxPrice: 200, startDate: '', endDate: '' });
        setAvailableCarIds(null);
    };

    const uniqueBrands = [...new Set(cars.map(car => car.brand))].sort();

    const filteredCars = cars.filter(car => {
        const matchBrand = filters.brand === 'all' || car.brand.toLowerCase() === filters.brand.toLowerCase();
        const matchPrice = car.pricePerDay <= filters.maxPrice;
        const matchDate = availableCarIds === null || availableCarIds.includes(car.id);
        return matchBrand && matchPrice && matchDate;
    });

    if (loading) return <div style={{padding: '100px', textAlign: 'center'}}>Завантаження автопарку... 🚗</div>;
    if (error) return <div style={{padding: '100px', textAlign: 'center', color: 'red'}}>{error}</div>;

    return (
        <div className={styles.pageContainer}>
            <div className={styles.heroSection}>
                <h1 className={styles.heroTitle}>Оренда автомобілів<br />– від <span className={styles.highlight}>15€ на добу</span></h1>
                <p className={styles.heroSubtitle}>Пн-Нд, 24/7</p>
            </div>

            <div className={styles.catalogSection}>
                <h2 className={styles.sectionTitle}>Наш автопарк</h2>

                <div className={styles.catalogLayout}>
                    <aside className={styles.filterSidebar}>
                        <div className={styles.filterHeader}>
                            Фільтри
                            <button className={styles.resetBtn} onClick={resetFilters}>Скинути</button>
                        </div>
                        <div className={styles.filterGroup}>
                            <label>Марка авто</label>
                            <select name="brand" value={filters.brand} onChange={handleFilterChange}>
                                <option value="all">Всі марки</option>
                                {uniqueBrands.map(brand => (
                                    <option key={brand} value={brand.toLowerCase()}>{brand}</option>
                                ))}
                            </select>
                        </div>
                        <div className={styles.filterGroup}>
                            <label>Ціна до: {filters.maxPrice}€ / доба</label>
                            <input type="range" name="maxPrice" min="10" max="200" step="5" value={filters.maxPrice} onChange={handleFilterChange} />
                        </div>

                        <div className={styles.filterGroup}>
                            <label>🗓️ Період доступності</label>
                            <div style={{ marginTop: '10px' }}>
                                <label style={{ fontSize: '13px', color: '#666', display: 'block', marginBottom: '4px' }}>Початок</label>
                                <input type="date" name="startDate" min={todayStr} value={filters.startDate} onChange={handleFilterChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', fontFamily: 'inherit' }} />
                            </div>
                            <div style={{ marginTop: '10px' }}>
                                <label style={{ fontSize: '13px', color: '#666', display: 'block', marginBottom: '4px' }}>Завершення</label>
                                <input type="date" name="endDate" min={filters.startDate || todayStr} value={filters.endDate} onChange={handleFilterChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', fontFamily: 'inherit' }} />
                            </div>
                        </div>
                    </aside>

                    <div className={styles.carGrid}>
                        {filteredCars.length > 0 ? (
                            filteredCars.map(car => (
                                <div key={car.id} className={styles.carCard}>
                                    <div className={styles.cardHeader}>
                                        <div>
                                            <h3 className={styles.carBrand}>{car.brand} {car.model}</h3>
                                            <div className={styles.carSubtitle}>{car.year} рік</div>
                                        </div>
                                        <div className={styles.carPriceBlock}>
                                            <div className={styles.carPrice}>Від {car.pricePerDay}€ </div>
                                            <span className={styles.priceNote}>за добу прокату</span>
                                        </div>
                                    </div>
                                    <div className={styles.imageGallery}>
                                        {/* 👑 КРИТИЧНИЙ ФІКС: Повернули оригінальний клас стилів для плейсхолдера */}
                                        <div className={styles.mainImagePlaceholder} style={{ background: '#f9f9f9', height: '220px' }}>
                                            <SecureImage src={`/car/v1/${car.id}/images/main`} alt={`${car.brand} ${car.model}`} style={{ width: '100%', height: '100%', borderRadius: '6px' }} />
                                        </div>
                                    </div>
                                    <ul className={styles.specsList}>
                                        <li><span className={styles.specLabel}>Клас:</span> {car.carClass}</li>
                                        <li><span className={styles.specLabel}>Статус:</span> {car.status}</li>
                                        <li><span className={styles.specLabel}>Коробка:</span> Автомат </li>
                                    </ul>
                                    <div className={styles.cardActions}>
                                        <button className={styles.bookBtn} onClick={() => navigate(`/book/${car.id}`)}>🚗 Забронювати</button>
                                        <Link to={`/catalog/${car.id}`} className={styles.detailsBtn}>ⓘ Детальніше</Link>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className={styles.noResults}><h3>Автомобілів не знайдено або всі зайняті 😕</h3></div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CarCatalogPage;
