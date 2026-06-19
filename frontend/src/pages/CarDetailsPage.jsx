import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './CarDetailsPage.module.css';
import { carService } from '../services/car.service';
import SecureImage from '../components/SecureImage';

const CarDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [car, setCar] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [images, setImages] = useState([]);
    const [activeImageId, setActiveImageId] = useState(null);
    const [imagesLoading, setImagesLoading] = useState(true);

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

    useEffect(() => {
        if (!car) return;

        const loadCarGallery = async () => {
            try {
                setImagesLoading(true);
                const imageData = await carService.getCarImages(car.id);
                setImages(imageData || []);

                const mainImg = imageData.find(img => img.isMain) || imageData[0];
                if (mainImg) {
                    setActiveImageId(mainImg.id);
                }
            } catch (err) {
                console.error("Не вдалося завантажити фото авто:", err);
            } finally {
                setImagesLoading(false);
            }
        };

        loadCarGallery();
    }, [car]);

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

                    {/* Велике активне фото з безпечним стрімінгом */}
                    <div className={styles.mainImagePlaceholder} style={{ background: '#f5f5f5', height: '380px', borderRadius: '8px', overflow: 'hidden' }}>
                        {activeImageId ? (
                            <SecureImage
                                src={`/car/v1/${car.id}/images/${activeImageId}`}
                                alt={`${car.brand} ${car.model}`}
                                style={{ width: '100%', height: '100%' }}
                            />
                        ) : (
                            <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#666' }}>
                                🚗 Медіафайли завантажуються...
                            </div>
                        )}
                    </div>

                    {/* Динамічна інтерактивна стрічка мініатюр знизу */}
                    <div className={styles.thumbnailGallery} style={{ display: 'flex', gap: '10px', marginTop: '12px', overflowX: 'auto', paddingBottom: '5px' }}>
                        {images.map(img => (
                            <div
                                key={img.id}
                                onClick={() => setActiveImageId(img.id)}
                                style={{
                                    width: '85px', height: '55px', flexShrink: 0, cursor: 'pointer',
                                    borderRadius: '6px', overflow: 'hidden',
                                    border: img.id === activeImageId ? '2.5px solid #0056b3' : '1.5px solid #ddd',
                                    transform: img.id === activeImageId ? 'scale(0.96)' : 'none',
                                    transition: 'all 0.15s ease-in-out'
                                }}
                            >
                                <SecureImage
                                    src={`/car/v1/${car.id}/images/${img.id}`}
                                    alt="Thumbnail"
                                    style={{ width: '100%', height: '100%' }}
                                />
                            </div>
                        ))}
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
                        <button className={styles.bookBtn} onClick={() => navigate(`/book/${car.id}`)}>
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
