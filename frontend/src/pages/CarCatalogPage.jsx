import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './CarCatalogPage.module.css';
import { carService } from '../services/car.service';
import { bookingService } from '../services/booking.service';
import SecureImage from '../components/SecureImage';
import { toast } from 'react-toastify';

// Імпортуємо новий автомобільний Hero-банер
import catalogBanner from '../assets/catalog-banner.png';

const CarCatalogPage = () => {
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const [availableCarIds, setAvailableCarIds] = useState(null);
    const todayStr = new Date().toLocaleDateString('en-CA');

    const [filters, setFilters] = useState(() => {
        const saved = localStorage.getItem('carFilters');
        return saved ? JSON.parse(saved) : {
            brand: 'all',
            carClass: 'all',
            model: '',
            year: '',
            maxPrice: 200,
            startDate: '',
            endDate: '',
            sortBy: 'newest'
        };
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
        setFilters({
            brand: 'all',
            carClass: 'all',
            model: '',
            year: '',
            maxPrice: 200,
            startDate: '',
            endDate: '',
            sortBy: 'newest'
        });
        setAvailableCarIds(null);
    };

    const uniqueBrands = [...new Set(cars.map(car => car.brand).filter(Boolean))].sort();

    const filteredCars = cars.filter(car => {
        const matchBrand = filters.brand === 'all' || (car.brand && car.brand.toLowerCase() === filters.brand.toLowerCase());
        const matchClass = filters.carClass === 'all' || (car.carClass && car.carClass.toUpperCase() === filters.carClass.toUpperCase());
        const matchModel = !filters.model || (car.model && car.model.toLowerCase().includes(filters.model.toLowerCase().trim()));
        const matchYear = !filters.year || (car.year && car.year.toString() === filters.year.trim());
        const matchPrice = car.pricePerDay <= filters.maxPrice;
        const matchDate = availableCarIds === null || availableCarIds.includes(car.id);

        return matchBrand && matchClass && matchModel && matchYear && matchPrice && matchDate;
    });

    const sortedAndFilteredCars = [...filteredCars].sort((a, b) => {
        if (filters.sortBy === 'price-asc') return a.pricePerDay - b.pricePerDay;
        if (filters.sortBy === 'price-desc') return b.pricePerDay - a.pricePerDay;
        if (filters.sortBy === 'year-desc') return b.year - a.year;
        if (filters.sortBy === 'newest') return b.id - a.id;
        return 0;
    });

    if (loading) return <div style={{padding: '100px', textAlign: 'center'}}>Завантаження автопарку... 🚗</div>;
    if (error) return <div style={{padding: '100px', textAlign: 'center', color: 'red'}}>{error}</div>;

    return (
        <div className={styles.pageContainer} style={{ maxWidth: '1300px', margin: '0 auto', padding: '0 20px' }}>

            {/* 👑 КРИТИЧНИЙ ФІКС ВЕРСТКИ: Банер тепер високий, просторий, а машина по центру! */}
            <div
                className={styles.heroSection}
                style={{
                    backgroundImage: `linear-gradient(to right, rgba(0, 0, 0, 0.7) 30%, rgba(0, 0, 0, 0.2) 70%), url(${catalogBanner})`,
                    backgroundSize: 'cover',
                    backgroundPosition: 'center center',
                    color: '#fff',
                    minHeight: '280px',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'flex-start',
                    padding: '40px 60px',
                    borderRadius: '16px',
                    marginBottom: '40px',
                    boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
                    boxSizing: 'border-box'
                }}
            >
                <h1 className={styles.heroTitle} style={{ color: '#fff', textShadow: '0 2px 10px rgba(0,0,0,0.6)', fontSize: '36px', fontWeight: '800', margin: 0, lineHeight: '1.25' }}>
                    Оренда автомобілів<br />– від <span className={styles.highlight} style={{ color: '#3ba4f6' }}>15€ на добу</span>
                </h1>
                <p className={styles.heroSubtitle} style={{ color: '#e0e0e0', textShadow: '0 1px 5px rgba(0,0,0,0.5)', marginTop: '12px', fontSize: '15px', fontWeight: '500', marginBottom: 0 }}>
                    📍 Пн-Нд, 24/7 — Твій швидкий та надійний рух
                </p>
            </div>

            <div className={styles.catalogSection}>
                <h2 className={styles.sectionTitle} style={{ fontSize: '24px', fontWeight: '700', marginBottom: '25px', color: '#1a1a1a' }}>Наш автопарк</h2>

                <div className={styles.catalogLayout}>
                    <aside className={styles.filterSidebar}>
                        <div className={styles.filterHeader}>
                            Фільтри
                            <button className={styles.resetBtn} onClick={resetFilters}>Скинути</button>
                        </div>

                        <div className={styles.filterGroup} style={{ marginBottom: '15px', borderBottom: '1px solid #eee', paddingBottom: '15px' }}>
                            <label style={{ fontWeight: 'bold', color: '#0056b3' }}>↕ Сортувати за</label>
                            <select name="sortBy" value={filters.sortBy} onChange={handleFilterChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', marginTop: '5px' }}>
                                <option value="newest">Новизною оголошення</option>
                                <option value="price-asc">Ціною: від дешевих до дорогих</option>
                                <option value="price-desc">Ціною: від дорогих до дешевих</option>
                                <option value="year-desc">Роком випуску: спочатку нові</option>
                            </select>
                        </div>

                        <div className={styles.filterGroup}>
                            <label>Марка авто</label>
                            <select name="brand" value={filters.brand} onChange={handleFilterChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }}>
                                <option value="all">Всі марки</option>
                                {uniqueBrands.map(brand => (
                                    <option key={brand} value={brand.toLowerCase()}>{brand}</option>
                                ))}
                            </select>
                        </div>

                        <div className={styles.filterGroup} style={{ marginTop: '10px' }}>
                            <label>Модель автомобіля</label>
                            <input type="text" name="model" value={filters.model} onChange={handleFilterChange} placeholder="Напр. Camry, Civic..." style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', marginTop: '5px' }} />
                        </div>

                        <div className={styles.filterGroup} style={{ marginTop: '10px' }}>
                            <label>Клас авто</label>
                            <select name="carClass" value={filters.carClass} onChange={handleFilterChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', marginTop: '5px' }}>
                                <option value="all">Всі класи</option>
                                <option value="ECONOMY">Economy</option>
                                <option value="COMFORT">Comfort</option>
                                <option value="BUSINESS">Business</option>
                                <option value="LUXURY">Luxury</option>
                            </select>
                        </div>

                        <div className={styles.filterGroup} style={{ marginTop: '10px' }}>
                            <label>Рік випуску</label>
                            <input type="number" name="year" min="1950" max="2027" value={filters.year} onChange={handleFilterChange} placeholder="Напр. 2024" style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px', marginTop: '5px' }} />
                        </div>

                        <div className={styles.filterGroup} style={{ marginTop: '10px' }}>
                            <label>Ціна до: {filters.maxPrice}€ / доба</label>
                            <input type="range" name="maxPrice" min="10" max="200" step="5" value={filters.maxPrice} onChange={handleFilterChange} style={{ width: '100%' }} />
                        </div>

                        <div className={styles.filterGroup} style={{ borderTop: '1px solid #eee', paddingTop: '15px', marginTop: '15px' }}>
                            <label style={{ fontWeight: 'bold', color: '#0056b3' }}>📅 Період доступності</label>
                            <div style={{ marginTop: '10px' }}>
                                <label style={{ fontSize: '13px', color: '#666', display: 'block', marginBottom: '4px' }}>Початок</label>
                                <input type="date" name="startDate" min={todayStr} value={filters.startDate} onChange={handleFilterChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }} />
                            </div>
                            <div style={{ marginTop: '10px' }}>
                                <label style={{ fontSize: '13px', color: '#666', display: 'block', marginBottom: '4px' }}>Завершення</label>
                                <input type="date" name="endDate" min={filters.startDate || todayStr} value={filters.endDate} onChange={handleFilterChange} style={{ width: '100%', padding: '8px', border: '1px solid #ccc', borderRadius: '4px' }} />
                            </div>
                        </div>
                    </aside>

                    <div className={styles.carGrid}>
                        {sortedAndFilteredCars.length > 0 ? (
                            sortedAndFilteredCars.map(car => (
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
                            <div className={styles.noResults}><h3>Automobiles matching these criteria were not found 😕</h3></div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CarCatalogPage;
