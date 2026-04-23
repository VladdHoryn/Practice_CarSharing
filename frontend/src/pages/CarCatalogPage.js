import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './CarCatalogPage.module.css';
import { carService } from '../services/car.service';

const CarCatalogPage = () => {
    const [cars, setCars] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const [filters, setFilters] = useState({
        brand: 'all',
        maxPrice: 200 // Максимальна ціна за замовчуванням
    });

    // Завантажуємо реальні дані з бекенду (Spring Boot)
    useEffect(() => {
        const fetchCars = async () => {
            try {
                setLoading(true);
                // Звертаємось до нашого Java-бекенду
                const data = await carService.getAvailableCars();
                setCars(data);
                setError(null);
            } catch (err) {
                console.error('Помилка завантаження авто:', err);
                setError('Не вдалося зв\'язатися з сервером. Переконайтеся, що car-service запущено на порту 8085.');
            } finally {
                setLoading(false);
            }
        };

        fetchCars();
    }, []);

    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({ ...prev, [name]: value }));
    };

    const resetFilters = () => {
        setFilters({ brand: 'all', maxPrice: 100 });
    };

    // Фільтруємо реальні дані
    const filteredCars = cars.filter(car => {
        const matchBrand = filters.brand === 'all' || car.brand.toLowerCase().includes(filters.brand.toLowerCase());
        const matchPrice = car.pricePerDay <= filters.maxPrice; // Використовуємо pricePerDay з твого DTO
        return matchBrand && matchPrice;
    });

    if (loading) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center'}}>Завантаження автопарку... 🚗</div>;
    if (error) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center', color: 'red'}}>{error}</div>;

    return (
        <div className={styles.pageContainer}>
            <div className={styles.heroSection}>
                <h1 className={styles.heroTitle}>Оренда автомобілів<br />– від <span className={styles.highlight}>15€ на добу</span></h1>
                <p className={styles.heroSubtitle}>Пн-Нд, 24/7</p>
            </div>

            <div className={styles.catalogSection}>
                <h2 className={styles.sectionTitle}>Наш автопарк</h2>

                <div className={styles.catalogLayout}>

                    {/* Спрощені фільтри (бо в БД поки немає типу коробки) */}
                    <aside className={styles.filterSidebar}>
                        <div className={styles.filterHeader}>
                            Фільтри
                            <button className={styles.resetBtn} onClick={resetFilters}>Скинути</button>
                        </div>
                        <div className={styles.filterGroup}>
                            <label>Марка авто</label>
                            <select name="brand" value={filters.brand} onChange={handleFilterChange}>
                                <option value="all">Всі марки</option>
                                <option value="toyota">Toyota</option>
                                <option value="bmw">BMW</option>
                                <option value="tesla">Tesla</option>
                            </select>
                        </div>
                        <div className={styles.filterGroup}>
                            <label>Ціна до: {filters.maxPrice}€ / доба</label>
                            <input type="range" name="maxPrice" min="10" max="200" step="5" value={filters.maxPrice} onChange={handleFilterChange} />
                        </div>
                    </aside>

                    <div className={styles.carGrid}>
                        {filteredCars.length > 0 ? (
                            filteredCars.map(car => (
                                <div key={car.id} className={styles.carCard}>
                                    <div className={styles.cardHeader}>
                                        <div>
                                            {/* Використовуємо поля з твого CarResponse */}
                                            <h3 className={styles.carBrand}>{car.brand} {car.model}</h3>
                                            <div className={styles.carSubtitle}>{car.year} рік</div>
                                        </div>
                                        <div className={styles.carPriceBlock}>
                                            <div className={styles.carPrice}>Від {car.pricePerDay}€ </div>
                                            <span className={styles.priceNote}>за добу прокату</span>
                                        </div>
                                    </div>

                                    <div className={styles.imageGallery}>
                                        <div className={styles.mainImagePlaceholder}>
                                            {/* Якщо бекенд повертає URL, показуємо картинку */}
                                            {car.imageUrl ? <img src={car.imageUrl} alt={car.brand} style={{width: '100%', height: '100%', objectFit: 'cover', borderRadius: '6px'}} /> : `[Фото ${car.brand}]`}
                                        </div>
                                    </div>

                                    <ul className={styles.specsList}>
                                        <li><span className={styles.specLabel}>Клас:</span> {car.carClass}</li>
                                        <li><span className={styles.specLabel}>Статус:</span> {car.status}</li>
                                        {/* Тимчасові заглушки для полів, яких ще немає в БД */}
                                        <li><span className={styles.specLabel}>Коробка:</span> Автомат (демо)</li>
                                    </ul>

                                    <div className={styles.cardActions}>
                                        <button className={styles.bookBtn} onClick={() => navigate(`/book/${car.id}`)}>
                                            🚗 Забронювати
                                        </button>
                                        <Link to={`/catalog/${car.id}`} className={styles.detailsBtn}>
                                            ⓘ Детальніше
                                        </Link>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <div className={styles.noResults}>
                                <h3>Автомобілів не знайдено 😕</h3>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CarCatalogPage;
