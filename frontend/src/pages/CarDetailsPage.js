import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './CarDetailsPage.module.css';
import { carService } from '../services/car.service';

const CarDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [car, setCar] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;
    const isOwner = user && user.role === 'OWNER';

    useEffect(() => {
        const fetchCarDetails = async () => {
            try {
                setLoading(true);
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

    if (loading) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center'}}>Завантаження інформації про авто... ⏳</div>;
    if (error) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center', color: 'red'}}>{error}</div>;
    if (!car) return null;

    const basePrice = car.pricePerDay;
    const pricing = [
        { period: '1-3 доби', price: `${basePrice}€` },
        { period: '4-7 діб', price: `${Math.round(basePrice * 0.9)}€` },
        { period: '8-15 діб', price: `${Math.round(basePrice * 0.8)}€` },
        { period: '16+ діб', price: `${Math.round(basePrice * 0.7)}€` },
        { period: 'Депозит', price: '200€' }
    ];

    return (
        <div className={styles.pageContainer}>
            <div className={styles.topSection}>
                <div>
                    <div className={styles.carHeader}>
                        <h1 className={styles.carTitle}>{car.brand} {car.model}</h1>
                        <p className={styles.carSubtitle}>{car.year} рік, клас: {car.carClass}</p>
                    </div>

                    <div className={styles.mainImagePlaceholder}>
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

                    {isOwner ? (
                        <div className={styles.ownerWarning}>
                            ⚠️ Партнерам із роллю OWNER заборонено бронювати автомобілі.
                        </div>
                    ) : (
                        <button
                            className={styles.bookBtn}
                            onClick={() => navigate(`/book/${car.id}`)}
                        >
                            <span style={{fontSize: '18px'}}>⏱</span> ЗАБРОНЮВАТИ АВТО
                        </button>
                    )}

                    <div className={styles.ageNotice}>
                        <span>ⓘ</span>
                        <span className={styles.ageText}>Мінімальний вік водія - 23 роки</span>
                    </div>
                </div>
            </div>

            <div className={styles.bottomSection}>
                <div>
                    <h2 className={styles.sectionTitle}>Про цей автомобіль:</h2>
                    <p className={styles.descText}>
                        {car.brand} {car.model} ({car.year}) - це чудовий вибір у класі {car.carClass}.
                        Надійний, практичний та сучасний автомобіль для щоденних поїздок.
                    </p>
                </div>

                <div>
                    <h2 className={styles.sectionTitle}>Характеристики автомобіля:</h2>
                    <ul className={styles.specsList}>
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
