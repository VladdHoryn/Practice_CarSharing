import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './BookingPage.module.css';
import { carService } from '../services/car.service'; // Підключаємо сервіс!

const BookingPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    // Стейт для бронювання
    const [dates, setDates] = useState({ start: '', end: '' });
    const [paymentMethod, setPaymentMethod] = useState('CARD'); // CARD або CASH
    const [coDrivers, setCoDrivers] = useState([]); // Split Access

    // Стейт для реального авто з бекенду
    const [car, setCar] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Обчислення кількості днів
    const [days, setDays] = useState(0);

    // 1. Завантажуємо реальну машину з БД при відкритті сторінки
    useEffect(() => {
        const fetchCarForBooking = async () => {
            try {
                setLoading(true);
                const data = await carService.getCarById(id);
                setCar(data);
                setError(null);
            } catch (err) {
                console.error('Помилка завантаження авто для бронювання:', err);
                setError('Не вдалося завантажити дані автомобіля.');
            } finally {
                setLoading(false);
            }
        };

        fetchCarForBooking();
    }, [id]);

    // 2. Рахуємо дні при зміні дат
    useEffect(() => {
        if (dates.start && dates.end) {
            const start = new Date(dates.start);
            const end = new Date(dates.end);
            const diffTime = end - start;
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            setDays(diffDays > 0 ? diffDays : 0);
        } else {
            setDays(0);
        }
    }, [dates]);

    // Функції для Split Access
    const addCoDriver = () => {
        if (coDrivers.length < 4) {
            setCoDrivers([...coDrivers, { email: '', licenseNumber: '' }]);
        } else {
            alert('Досягнуто ліміт водіїв (максимум 5 на одне авто)');
        }
    };

    const removeCoDriver = (index) => {
        const newDrivers = coDrivers.filter((_, i) => i !== index);
        setCoDrivers(newDrivers);
    };

    const handleCoDriverChange = (index, field, value) => {
        const newDrivers = [...coDrivers];
        newDrivers[index][field] = value;
        setCoDrivers(newDrivers);
    };

    // Відправка форми
    const handleSubmit = (e) => {
        e.preventDefault();
        if (days <= 0) return alert('Оберіть коректні дати (дата завершення має бути пізніше дати початку)');

        const bookingData = {
            carId: car.id,
            startDate: dates.start,
            endDate: dates.end,
            paymentMethod,
            coDrivers,
            totalPrice: days * car.pricePerDay
        };

        // Тут ми пізніше підключимо booking.service.js
        console.log('Дані на бекенд (/api/bookings):', bookingData);
        alert('Бронювання успішно створено! (Дані виведено в консоль)');
        navigate('/catalog');
    };

    // Відображення під час очікування відповіді від сервера
    if (loading) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center'}}>Підготовка сторінки бронювання... ⏳</div>;
    if (error) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center', color: 'red'}}>{error}</div>;
    if (!car) return null;

    return (
        <div className={styles.pageContainer}>
            <h1 className={styles.pageTitle}>Оформлення бронювання</h1>

            <form onSubmit={handleSubmit} className={styles.contentGrid}>

                {/* ЛІВА КОЛОНКА (Форми) */}
                <div className={styles.leftColumn}>

                    {/* Блок 1: Дати */}
                    <div className={styles.formSection}>
                        <h2 className={styles.sectionTitle}>1. Дати оренди</h2>
                        <div className={styles.dateRow}>
                            <div className={styles.inputGroup}>
                                <label>Початок оренди</label>
                                <input
                                    type="date"
                                    required
                                    value={dates.start}
                                    onChange={(e) => setDates({...dates, start: e.target.value})}
                                />
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Завершення оренди</label>
                                <input
                                    type="date"
                                    required
                                    min={dates.start} // Не можна здати раніше, ніж взяти
                                    value={dates.end}
                                    onChange={(e) => setDates({...dates, end: e.target.value})}
                                />
                            </div>
                        </div>
                    </div>

                    {/* Блок 2: Split Access (Кілер-фіча) */}
                    <div className={`${styles.formSection} ${styles.splitAccessBox}`}>
                        <div className={styles.splitAccessHeader}>
                            <div>
                                <h2 className={styles.sectionTitle} style={{border: 'none', marginBottom: '5px'}}>
                                    2. Split Access (Спільна оренда)
                                </h2>
                                <p>Додайте друзів, щоб вони теж мали законне право керувати цим авто.</p>
                            </div>
                            <button type="button" className={styles.addDriverBtn} onClick={addCoDriver}>
                                + Додати водія
                            </button>
                        </div>

                        {coDrivers.map((driver, index) => (
                            <div key={index} className={styles.driverRow}>
                                <div className={styles.inputGroup}>
                                    <label>Email водія #{index + 2}</label>
                                    <input
                                        type="email"
                                        placeholder="friend@carlink.com"
                                        required
                                        value={driver.email}
                                        onChange={(e) => handleCoDriverChange(index, 'email', e.target.value)}
                                    />
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Номер посвідчення</label>
                                    <input
                                        type="text"
                                        placeholder="AAA123456"
                                        required
                                        value={driver.licenseNumber}
                                        onChange={(e) => handleCoDriverChange(index, 'licenseNumber', e.target.value)}
                                    />
                                </div>
                                <button type="button" className={styles.removeBtn} onClick={() => removeCoDriver(index)}>
                                    ✖
                                </button>
                            </div>
                        ))}
                        {coDrivers.length === 0 && (
                            <div style={{fontSize: '13px', color: '#666', fontStyle: 'italic'}}>
                                Ви будете єдиним водієм цього авто.
                            </div>
                        )}
                    </div>

                    {/* Блок 3: Оплата */}
                    <div className={styles.formSection}>
                        <h2 className={styles.sectionTitle}>3. Спосіб оплати</h2>
                        <div className={styles.paymentOptions}>
                            <label className={`${styles.paymentOption} ${paymentMethod === 'CARD' ? styles.selected : ''}`}>
                                <input
                                    type="radio"
                                    name="payment"
                                    checked={paymentMethod === 'CARD'}
                                    onChange={() => setPaymentMethod('CARD')}
                                />
                                💳 Оплата карткою онлайн
                            </label>
                            <label className={`${styles.paymentOption} ${paymentMethod === 'CASH' ? styles.selected : ''}`}>
                                <input
                                    type="radio"
                                    name="payment"
                                    checked={paymentMethod === 'CASH'}
                                    onChange={() => setPaymentMethod('CASH')}
                                />
                                💵 Готівкою при отриманні
                            </label>
                        </div>
                    </div>

                </div>

                {/* ПРАВА КОЛОНКА (Чек) */}
                <div>
                    <div className={styles.summaryCard}>
                        <h2 className={styles.sectionTitle}>Ваше замовлення</h2>

                        <div className={styles.summaryCar}>
                            {/* Відображаємо реальне фото з бекенду */}
                            <div className={styles.summaryCarImg}>
                                {car.imageUrl ? (
                                    <img src={car.imageUrl} alt={car.brand} style={{width: '100%', height: '100%', objectFit: 'cover', borderRadius: '4px'}}/>
                                ) : (
                                    <div style={{width: '100%', height: '100%', backgroundColor: '#eee', borderRadius: '4px', display: 'flex', alignItems: 'center', justifyContent: 'center'}}>Фото</div>
                                )}
                            </div>
                            <div>
                                {/* Відображаємо реальну марку і модель */}
                                <strong>{car.brand} {car.model}</strong>
                                <div style={{fontSize: '13px', color: '#666'}}>{car.pricePerDay}€ / доба</div>
                            </div>
                        </div>

                        <ul className={styles.summaryDetails}>
                            <li>
                                <span>Кількість днів:</span>
                                <span>{days}</span>
                            </li>
                            <li>
                                <span>Додаткові водії:</span>
                                <span>{coDrivers.length}</span>
                            </li>
                            <li>
                                <span>Страховка:</span>
                                <span style={{color: '#28a745'}}>Включено</span>
                            </li>
                        </ul>

                        <div className={styles.totalRow}>
                            <span>До сплати:</span>
                            <span>{days * car.pricePerDay}€</span>
                        </div>

                        <button type="submit" className={styles.confirmBtn}>
                            ПІДТВЕРДИТИ
                        </button>
                    </div>
                </div>

            </form>
        </div>
    );
};

export default BookingPage;
