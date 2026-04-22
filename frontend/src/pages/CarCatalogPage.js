import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './CarCatalogPage.module.css';

// Мокові дані
const MOCK_CARS = [
    {
        id: '1',
        brand: 'Dacia Logan',
        subtitle: 'Механіка, 1.2 л.',
        price: 15,
        specs: { engine: '1.2л (бензин)', consumption: '6л/100км', transmission: 'механічна', ac: 'ні', doors: 4 }
    },
    {
        id: '2',
        brand: 'Ford Fiesta',
        subtitle: 'Автомат, 1.6 л.',
        price: 20,
        specs: { engine: '1.6л (бензин)', consumption: '7л/100км', transmission: 'автомат', ac: 'так', doors: 5 }
    },
    {
        id: '3',
        brand: 'Peugeot 301',
        subtitle: 'Механіка, 1.2 л.',
        price: 18,
        specs: { engine: '1.2 л, 82 к.с., бензин', consumption: '6-8л/100км', transmission: 'механічна', ac: 'так', doors: 4 }
    },
    {
        id: '4',
        brand: 'Nissan Note',
        subtitle: 'Автомат, 1.6 л.',
        price: 25,
        specs: { engine: '1.6л., бензин', consumption: '6-8л/100км', transmission: 'автомат', ac: 'так', doors: 5 }
    }
];

const CarCatalogPage = () => {
    const [cars, setCars] = useState([]);
    const navigate = useNavigate();

    // 1. Стан для фільтрів
    const [filters, setFilters] = useState({
        brand: 'all',
        transmission: 'all',
        maxPrice: 50 // Початкова максимальна ціна (щоб показати всі авто)
    });

    useEffect(() => {
        // Імітація завантаження з бекенду
        setCars(MOCK_CARS);
    }, []);

    // 2. Обробник змін фільтрів
    const handleFilterChange = (e) => {
        const { name, value } = e.target;
        setFilters(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // 3. Скидання фільтрів
    const resetFilters = () => {
        setFilters({ brand: 'all', transmission: 'all', maxPrice: 50 });
    };

    // 4. Логіка фільтрації (Створюємо новий відфільтрований масив)
    const filteredCars = cars.filter(car => {
        // Перевірка марки (якщо 'all' - пропускаємо всі, інакше шукаємо збіг у назві)
        const matchBrand = filters.brand === 'all' || car.brand.toLowerCase().includes(filters.brand.toLowerCase());

        // Перевірка коробки передач
        const matchTransmission = filters.transmission === 'all' || car.specs.transmission.toLowerCase() === filters.transmission.toLowerCase();

        // Перевірка ціни
        const matchPrice = car.price <= filters.maxPrice;

        // Автомобіль має відповідати всім трьом умовам
        return matchBrand && matchTransmission && matchPrice;
    });

    return (
        <div className={styles.pageContainer}>

            {/* Банер */}
            <div className={styles.heroSection}>
                <h1 className={styles.heroTitle}>
                    Оренда автомобілів<br />
                    – від <span className={styles.highlight}>15€ на добу</span>
                </h1>
                <p className={styles.heroSubtitle}>Пн-Нд, 24/7</p>
            </div>

            <div className={styles.catalogSection}>
                <h2 className={styles.sectionTitle}>Наш автопарк</h2>

                {/* НОВИЙ ЛЕЙАУТ: Фільтри + Сітка */}
                <div className={styles.catalogLayout}>

                    {/* Бічна панель фільтрів */}
                    <aside className={styles.filterSidebar}>
                        <div className={styles.filterHeader}>
                            Фільтри
                            <button className={styles.resetBtn} onClick={resetFilters}>Скинути</button>
                        </div>

                        <div className={styles.filterGroup}>
                            <label>Марка авто</label>
                            <select name="brand" value={filters.brand} onChange={handleFilterChange}>
                                <option value="all">Всі марки</option>
                                <option value="dacia">Dacia</option>
                                <option value="ford">Ford</option>
                                <option value="peugeot">Peugeot</option>
                                <option value="nissan">Nissan</option>
                            </select>
                        </div>

                        <div className={styles.filterGroup}>
                            <label>Коробка передач</label>
                            <select name="transmission" value={filters.transmission} onChange={handleFilterChange}>
                                <option value="all">Будь-яка</option>
                                <option value="механічна">Механіка</option>
                                <option value="автомат">Автомат</option>
                            </select>
                        </div>

                        <div className={styles.filterGroup}>
                            <label>Ціна до: {filters.maxPrice}€ / доба</label>
                            <input
                                type="range"
                                name="maxPrice"
                                min="10"
                                max="50"
                                step="5"
                                value={filters.maxPrice}
                                onChange={handleFilterChange}
                            />
                        </div>
                    </aside>

                    {/* Сітка карток автомобілів */}
                    <div className={styles.carGrid}>
                        {/* Рендеримо filteredCars замість cars */}
                        {filteredCars.length > 0 ? (
                            filteredCars.map(car => (
                                <div key={car.id} className={styles.carCard}>
                                    <div className={styles.cardHeader}>
                                        <div>
                                            <h3 className={styles.carBrand}>{car.brand}</h3>
                                            <div className={styles.carSubtitle}>{car.subtitle}</div>
                                        </div>
                                        <div className={styles.carPriceBlock}>
                                            <div className={styles.carPrice}>Від {car.price}€ </div>
                                            <span className={styles.priceNote}>за добу прокату</span>
                                        </div>
                                    </div>

                                    <div className={styles.imageGallery}>
                                        <div className={styles.mainImagePlaceholder}>[Фото {car.brand}]</div>
                                        <div className={styles.thumbnailColumn}>
                                            <div className={styles.thumbPlaceholder}></div>
                                            <div className={styles.thumbPlaceholder}></div>
                                        </div>
                                    </div>

                                    <ul className={styles.specsList}>
                                        <li><span className={styles.specLabel}>Двигун:</span> {car.specs.engine}</li>
                                        <li><span className={styles.specLabel}>Витрата:</span> {car.specs.consumption}</li>
                                        <li><span className={styles.specLabel}>Коробка:</span> {car.specs.transmission}</li>
                                        <li><span className={styles.specLabel}>Кондиціонер:</span> {car.specs.ac}</li>
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
                            // Якщо жодне авто не підходить під фільтри
                            <div className={styles.noResults}>
                                <h3>Автомобілів не знайдено 😕</h3>
                                <p>Спробуйте змінити критерії пошуку або скинути фільтри.</p>
                            </div>
                        )}
                    </div>

                </div>
            </div>
        </div>
    );
};

export default CarCatalogPage;
