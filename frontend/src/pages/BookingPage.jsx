import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './BookingPage.module.css';
import { carService } from '../services/car.service';
import { bookingService } from '../services/booking.service';
import { paymentService } from '../services/payment.service';
import { toast } from 'react-toastify';

const BookingPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [dates, setDates] = useState({ start: '', end: '' });
    const [coDrivers, setCoDrivers] = useState([]);
    const [days, setDays] = useState(0);

    const [showPaymentModal, setShowPaymentModal] = useState(false);
    const [paymentMethod, setPaymentMethod] = useState('CARD');

    const [car, setCar] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isProcessing, setIsProcessing] = useState(false);

    const todayStr = new Date().toLocaleDateString('en-CA');

    useEffect(() => {
        const fetchCarForBooking = async () => {
            try {
                setLoading(true);
                const data = await carService.getCarById(id);
                setCar(data);
                setError(null);
            } catch (err) {
                console.error('Помилка завантаження авто:', err);
                setError('Не вдалося завантажити дані автомобіля.');
            } finally {
                setLoading(false);
            }
        };
        fetchCarForBooking();
    }, [id]);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (!storedUser) {
            navigate('/login');
            return;
        }

        const currentUser = JSON.parse(storedUser);
        if (currentUser.role === 'OWNER') {
            toast.error('Доступ заборонено: власники транспортних засобів не мають права створювати бронювання.', {
                toastId: 'owner-booking-block'
            });
            navigate('/catalog');
        }
    }, [navigate]);

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

    const addCoDriver = () => {
        if (coDrivers.length < 4) {
            setCoDrivers([...coDrivers, { email: '', licenseNumber: '' }]);
        } else {
            toast.warning('Досягнуто ліміт водіїв');
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

    const handleProceedToPayment = (e) => {
        e.preventDefault();
        if (days <= 0) {
            toast.error('Оберіть коректні дати оренди (мінімум 1 день)');
            return;
        }
        setShowPaymentModal(true);
    };

    const executeBookingAndPayment = async () => {
        const userStr = localStorage.getItem('user');
        if (!userStr) return navigate('/login');

        const currentUser = JSON.parse(userStr);

        if (!currentUser.dbId) {
                    toast.error('Помилка авторизації в базі даних. Спробуйте перелогінитися.');
                    return;
                }

        setIsProcessing(true);
        let createdBookingId = null;

        try {
            const bookingRequest = {
                userId: Number(currentUser.dbId),
                carId: Number(car.id),
                startDate: `${dates.start}T12:00:00`,
                endDate: `${dates.end}T12:00:00`,
                pricePerDay: Number(car.pricePerDay)
            };

            console.log("Відправляємо дані на бронювання:", bookingRequest);
            const bookingResult = await bookingService.createBooking(bookingRequest);
            createdBookingId = bookingResult.id;


            try {
                const paymentRequest = {
                    bookingId: createdBookingId,
                    amount: days * car.pricePerDay,
                    method: paymentMethod,
                    currency: "USD"
                };
                console.log("Відправляємо дані на оплату:", paymentRequest);
                await paymentService.createPayment(paymentRequest);
            } catch (paymentErr) {
                console.error("Помилка на етапі оплати. Запускаємо відкат бронювання...");


                if (createdBookingId) {
                    await bookingService.cancelBooking(createdBookingId);
                }

                const realError = paymentErr.response?.data?.message || paymentErr.message;
                throw new Error(`Оплату не виконано: ${realError}. Бронювання скасовано.`);
            }


            toast.success('Бронювання та оплата успішні! 🚗💳');
            setShowPaymentModal(false);
            navigate('/profile');

        } catch (err) {
            console.error("Помилка при виконанні операції:", err);

            const backendMessage = err.response?.data?.message || err.message;

            if (backendMessage.includes("already booked") || backendMessage.includes("overlap")) {
                toast.error("🚨 Цей автомобіль уже заброньовано на обрані дати! Будь ласка, змініть період оренди.");
            } else {
                toast.error(backendMessage || "Сталася непередбачувана помилка.");
            }
        } finally {
            setIsProcessing(false);
        }
    };

    if (loading) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center'}}>Підготовка сторінки... ⏳</div>;
    if (error) return <div className={styles.pageContainer} style={{padding: '100px', textAlign: 'center', color: 'red'}}>{error}</div>;
    if (!car) return null;

    const totalPrice = days * car.pricePerDay;

    return (
        <div className={styles.pageContainer}>
            <h1 className={styles.pageTitle}>Оформлення бронювання</h1>

            <form onSubmit={handleProceedToPayment} className={styles.contentGrid}>
                <div className={styles.leftColumn}>
                    <div className={styles.formSection}>
                        <h2 className={styles.sectionTitle}>1. Дати оренди</h2>
                        <div className={styles.dateRow}>
                            <div className={styles.inputGroup}>
                                <label>Початок оренди</label>
                                <input type="date" required min={todayStr} value={dates.start} onChange={(e) => setDates({...dates, start: e.target.value})} />
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Завершення оренди</label>
                                <input type="date" required min={dates.start || todayStr} value={dates.end} onChange={(e) => setDates({...dates, end: e.target.value})} />
                            </div>
                        </div>
                    </div>

                    <div className={`${styles.formSection} ${styles.splitAccessBox}`}>
                        <div className={styles.splitAccessHeader}>
                            <div>
                                <h2 className={styles.sectionTitle} style={{border: 'none', marginBottom: '5px'}}>2. Split Access (Спільна оренда)</h2>
                                <p>Додайте друзів, щоб вони теж мали законне право керувати цим авто.</p>
                            </div>
                            <button type="button" className={styles.addDriverBtn} onClick={addCoDriver}>+ Додати водія</button>
                        </div>
                        {coDrivers.map((driver, index) => (
                            <div key={index} className={styles.driverRow}>
                                <div className={styles.inputGroup}>
                                    <label>Email водія #{index + 2}</label>
                                    <input type="email" required value={driver.email} onChange={(e) => handleCoDriverChange(index, 'email', e.target.value)} />
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Номер посвідчення</label>
                                    <input type="text" required value={driver.licenseNumber} onChange={(e) => handleCoDriverChange(index, 'licenseNumber', e.target.value)} />
                                </div>
                                <button type="button" className={styles.removeBtn} onClick={() => removeCoDriver(index)}>✖</button>
                            </div>
                        ))}
                        {coDrivers.length === 0 && <div style={{fontSize: '13px', color: '#666', fontStyle: 'italic'}}>Ви будете єдиним водієм цього авто.</div>}
                    </div>
                </div>

                <div className={styles.summaryCard}>
                    <h2 className={styles.sectionTitle}>Ваше замовлення</h2>
                    <div className={styles.summaryCar}>
                        <div className={styles.summaryCarImg}>
                            {car.imageUrl ? <img src={car.imageUrl} alt={car.brand} style={{width: '100%', height: '100%', objectFit: 'cover', borderRadius: '4px'}}/> : <div style={{width: '100%', height: '100%', backgroundColor: '#eee'}}>Фото</div>}
                        </div>
                        <div>
                            <strong>{car.brand} {car.model}</strong>
                            <div style={{fontSize: '13px', color: '#666'}}>{car.pricePerDay}€ / доба</div>
                        </div>
                    </div>
                    <ul className={styles.summaryDetails}>
                        <li><span>Кількість днів:</span><span>{days}</span></li>
                        <li><span>Додаткові водії:</span><span>{coDrivers.length}</span></li>
                        <li><span>Страховка:</span><span style={{color: '#28a745'}}>Включено</span></li>
                    </ul>
                    <div className={styles.totalRow}><span>До сплати:</span><span>{totalPrice}€</span></div>

                    <button type="submit" className={styles.confirmBtn}>ПЕРЕЙТИ ДО ОПЛАТИ</button>
                </div>
            </form>

            {showPaymentModal && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
                    <div style={{ background: '#fff', padding: '30px', borderRadius: '12px', width: '450px', maxWidth: '90%', boxShadow: '0 10px 25px rgba(0,0,0,0.2)' }}>
                        <h2 style={{marginTop: 0, marginBottom: '20px', fontSize: '22px'}}>Оплата замовлення</h2>

                        <div style={{ marginBottom: '20px' }}>
                            <label style={{ display: 'flex', alignItems: 'center', padding: '15px', border: paymentMethod === 'CARD' ? '2px solid #0056b3' : '1px solid #ddd', borderRadius: '8px', marginBottom: '10px', cursor: 'pointer', background: paymentMethod === 'CARD' ? '#f8fbff' : '#fff' }}>
                                <input type="radio" name="payMethod" checked={paymentMethod === 'CARD'} onChange={() => setPaymentMethod('CARD')} style={{marginRight: '15px'}} />
                                💳 Банківська картка
                            </label>

                            {paymentMethod === 'CARD' && (
                                <div style={{ padding: '0 10px 15px 35px' }}>
                                    <input type="text" placeholder="Номер картки 0000 0000 0000 0000" style={{ width: '100%', padding: '10px', marginBottom: '10px', border: '1px solid #ccc', borderRadius: '4px' }}/>
                                    <div style={{ display: 'flex', gap: '10px' }}>
                                        <input type="text" placeholder="MM/YY" style={{ width: '50%', padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}/>
                                        <input type="text" placeholder="CVC" style={{ width: '50%', padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}/>
                                    </div>
                                </div>
                            )}

                            <label style={{ display: 'flex', alignItems: 'center', padding: '15px', border: paymentMethod === 'GOOGLE_PAY' ? '2px solid #0056b3' : '1px solid #ddd', borderRadius: '8px', marginBottom: '10px', cursor: 'pointer', background: paymentMethod === 'GOOGLE_PAY' ? '#f8fbff' : '#fff' }}>
                                <input type="radio" name="payMethod" checked={paymentMethod === 'GOOGLE_PAY'} onChange={() => setPaymentMethod('GOOGLE_PAY')} style={{marginRight: '15px'}} />
                                📱 Google Pay
                            </label>

                            <label style={{ display: 'flex', alignItems: 'center', padding: '15px', border: paymentMethod === 'APPLE_PAY' ? '2px solid #0056b3' : '1px solid #ddd', borderRadius: '8px', cursor: 'pointer', background: paymentMethod === 'APPLE_PAY' ? '#f8fbff' : '#fff' }}>
                                <input type="radio" name="payMethod" checked={paymentMethod === 'APPLE_PAY'} onChange={() => setPaymentMethod('APPLE_PAY')} style={{marginRight: '15px'}} />
                                🍎 Apple Pay
                            </label>
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '30px' }}>
                            <button onClick={() => setShowPaymentModal(false)} style={{ padding: '12px 20px', border: 'none', background: '#eee', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}>
                                Скасувати
                            </button>
                            <button onClick={executeBookingAndPayment} disabled={isProcessing} style={{ padding: '12px 20px', border: 'none', background: '#0056b3', color: '#fff', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', width: '200px' }}>
                                {isProcessing ? 'Обробка...' : `Оплатити ${totalPrice}€`}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default BookingPage;
