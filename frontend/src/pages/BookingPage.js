import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './BookingPage.module.css';

const BookingPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    // Стейт для бронювання
    const [dates, setDates] = useState({ start: '', end: '' });
    const [paymentMethod, setPaymentMethod] = useState('CARD'); // CARD або CASH
    const [coDrivers, setCoDrivers] = useState([]); // Split Access

    // Мокові дані авто
    const car = { id: id, brand: 'Dacia Logan', pricePerDay: 29 };

    // Обчислення кількості днів та загальної суми
    const [days, setDays] = useState(0);

    useEffect(() => {
        if (dates.start && dates.end) {
            const start = new Date(dates.start);
            const end = new Date(dates.end);
            const diffTime = Math.abs(end - start);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            setDays(diffDays > 0 ? diffDays : 0);
        } else {
            setDays(0);
        }
    }, [dates]);

    // Функції для Split Access
    const addCoDriver = () => {
        if (coDrivers.length < 4) { // Максимум 5 водіїв (1 основний + 4 додаткових)
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
        if (days <= 0) return alert('Оберіть коректні дати');

        const bookingData = {
            carId: car.id,
            startDate: dates.start,
            endDate: dates.end,
            paymentMethod,
            coDrivers,
            totalPrice: days * car.pricePerDay
        };

        console.log('Дані на бекенд (/api/bookings):', bookingData);
        alert('Бронювання успішно створено!');
        navigate('/catalog');
    };

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
                            <div className={styles.summaryCarImg}></div>
                            <div>
                                <strong>{car.brand}</strong>
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
