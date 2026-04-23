import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './CarDetailsPage.module.css';
import { carService } from '../services/car.service'; // Підключаємо наш реальний сервіс

const CarDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    // Додаємо стани для завантаження та помилок
    const [car, setCar] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchCarDetails = async () => {
            try {
                setLoading(true);
                // Робимо запит до бекенду: GET /car/v1/{id}
                const data = await carService.getCarById(id);
                setCar(data);
                setError(null);
            } catch (err) {
                console.error('Помилка завантаження деталей авто:', err);
                setError('На жаль, не вдалося знайти інформацію про цей автомобіль.');
            } finally {
                setLoading(false);
            }
        };

        fetchCarDetails();
    }, [id]);

    // Відображення під час очікування відповіді від сервера
    if (loading) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center'}}>Завантаження інформації про авто... ⏳</div>;
    if (error) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center', color: 'red'}}>{error}</div>;
    if (!car) return null;

    // Розраховуємо ціни зі знижками на основі базової ціни з БД
    const basePrice = car.pricePerDay;
    const pricing = [
        { period: '1-3 доби', price: `${basePrice}€` },
        { period: '4-7 діб', price: `${Math.round(basePrice * 0.9)}€` }, // 10% знижка
        { period: '8-15 діб', price: `${Math.round(basePrice * 0.8)}€` }, // 20% знижка
        { period: '16+ діб', price: `${Math.round(basePrice * 0.7)}€` }, // 30% знижка
        { period: 'Депозит', price: '200€' }
    ];

    return (
        <div className={styles.pageContainer}>

            {/* ВЕРХНЯ СЕКЦІЯ */}
            <div className={styles.topSection}>

                {/* Ліва колонка: Назва та Фото */}
                <div>
                    <div className={styles.carHeader}>
                        {/* Підтягуємо реальні марку та модель */}
                        <h1 className={styles.carTitle}>{car.brand} {car.model}</h1>
                        <p className={styles.carSubtitle}>{car.year} рік, клас: {car.carClass}</p>
                    </div>

                    <div className={styles.mainImagePlaceholder}>
                        {/* Якщо в базі є лінк на фото - показуємо його, інакше заглушку */}
                        {car.imageUrl ? (
                            <img src={car.imageUrl} alt={`${car.brand} ${car.model}`} style={{width: '100%', height: '100%', objectFit: 'cover', borderRadius: '4px'}}/>
                        ) : (
                            `[Фото авто: ${car.brand} ${car.model}]`
                        )}
                    </div>

                    <div className={styles.thumbnailGallery}>
                        <div className={styles.thumbnail}></div>
                        <div className={styles.thumbnail}></div>
                    </div>
                </div>

                {/* Права колонка: Таблиця цін */}
                <div className={styles.pricingBlock}>
                    <h2 className={styles.sectionTitle}>Ціна оренди</h2>
                    <p className={styles.sectionSubtitle}>Вартість залежить від терміну оренди</p>

                    <ul className={styles.priceTable}>
                        {pricing.map((item, index) => (
                            <li key={index} className={styles.priceRow}>
                                <span>{item.period}</span>
                                <span className={styles.priceValue}>{item.price}</span>
                            </li>
                        ))}
                    </ul>

                    <button
                        className={styles.bookBtn}
                        onClick={() => navigate(`/book/${car.id}`)}
                    >
                        <span style={{fontSize: '18px'}}>⏱</span> ЗАБРОНЮВАТИ АВТО
                    </button>

                    <div className={styles.ageNotice}>
                        <span>ⓘ</span>
                        <span className={styles.ageText}>Мінімальний вік водія - 23 роки</span>
                    </div>
                </div>

            </div>

            {/* НИЖНЯ СЕКЦІЯ */}
            <div className={styles.bottomSection}>

                {/* Ліва колонка: Опис */}
                <div>
                    <h2 className={styles.sectionTitle}>Про цей автомобіль:</h2>
                    <p className={styles.descText}>
                        {car.brand} {car.model} ({car.year}) - це чудовий вибір у класі {car.carClass}.
                        Надійний, практичний та сучасний автомобіль для щоденних поїздок.
                        Відмінно підійде як для їзди по місту, так і для тривалих подорожей з сім'єю чи друзями.
                    </p>
                </div>

                {/* Права колонка: Характеристики */}
                <div>
                    <h2 className={styles.sectionTitle}>Характеристики автомобіля:</h2>
                    <ul className={styles.specsList}>
                        {/* Ці поля поки захардкоджені, бо їх ще немає в DTO/Entity */}
                        <li><strong>Двигун:</strong> 2.0 л., бензин (демо)</li>
                        <li><strong>Витрата пального:</strong> 7л/100км (демо)</li>
                        <li><strong>Коробка:</strong> Автомат (демо)</li>
                        <li><strong>Кондиціонер:</strong> так (демо)</li>
                        <li><strong>Клас авто:</strong> {car.carClass}</li>
                    </ul>
                </div>

            </div>

        </div>
    );
};

export default CarDetailsPage;
