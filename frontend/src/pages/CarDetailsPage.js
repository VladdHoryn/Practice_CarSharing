import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './CarDetailsPage.module.css';

const CarDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [car, setCar] = useState(null);

    useEffect(() => {
        // Тимчасові мокові дані, що ідеально підходять під макет
        const mockCarDetails = {
            id: id || '1',
            brand: 'Dacia Logan',
            subtitle: 'Механічна, 1.2 л.',
            pricing: [
                { period: '1-3 доби', price: '29€' },
                { period: '4-7 діб', price: '25€' }, // Зробив трохи реалістичніше за макет
                { period: '8-15 діб', price: '22€' },
                { period: '16+ діб', price: '19€' },
                { period: 'Депозит', price: '200€' }
            ],
            description: 'Dacia Logan - це надійний, практичний, сучасний автомобіль для щоденних поїздок. Відмінно підійде як для їзди по місту, так і для тривалих подорожей. Компактний седан з містким багажником відмінно підійде для поїздок з сім\'єю і друзями.',
            specs: {
                engine: '1.2 л., 92 к.с., бензин',
                consumption: '6-8л/100км',
                transmission: 'механічна',
                ac: 'так',
                doors: '4'
            }
        };
        setCar(mockCarDetails);
    }, [id]);

    if (!car) return <div className={styles.pageContainer}>Завантаження...</div>;

    return (
        <div className={styles.pageContainer}>

            {/* ВЕРХНЯ СЕКЦІЯ */}
            <div className={styles.topSection}>

                {/* Ліва колонка: Назва та Фото */}
                <div>
                    <div className={styles.carHeader}>
                        <h1 className={styles.carTitle}>{car.brand}</h1>
                        <p className={styles.carSubtitle}>{car.subtitle}</p>
                    </div>

                    <div className={styles.mainImagePlaceholder}>
                        {/* <img src={car.imageUrl} alt={car.brand} style={{width: '100%', borderRadius: '4px'}}/> */}
                        [Фото авто: {car.brand}]
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
                        {car.pricing.map((item, index) => (
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
                        {car.description}
                    </p>
                </div>

                {/* Права колонка: Характеристики */}
                <div>
                    <h2 className={styles.sectionTitle}>Характеристики автомобіля:</h2>
                    <ul className={styles.specsList}>
                        <li><strong>Двигун:</strong> {car.specs.engine}</li>
                        <li><strong>Витрата пального:</strong> {car.specs.consumption}</li>
                        <li><strong>Коробка:</strong> {car.specs.transmission}</li>
                        <li><strong>Кондиціонер:</strong> {car.specs.ac}</li>
                        <li><strong>Дверей:</strong> {car.specs.doors}</li>
                    </ul>
                </div>

            </div>

        </div>
    );
};

export default CarDetailsPage;
