import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import styles from './BookingPage.module.css';
import { carService } from '../services/car.service';
import { bookingService } from '../services/booking.service';
import { paymentService } from '../services/payment.service';
import { toast } from 'react-toastify';
import SecureImage from '../components/SecureImage';

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

    const [occupiedDates, setOccupiedDates] = useState([]);
    const [currentField, setCurrentField] = useState(null);
    const [viewDate, setViewDate] = useState(new Date());
    const calendarRef = useRef(null);

    const todayStr = new Date().toLocaleDateString('en-CA');

    useEffect(() => {
        const fetchCarAndBookingData = async () => {
            try {
                setLoading(true);
                const carData = await carService.getCarById(id);
                setCar(carData);
                const bookedPeriods = await bookingService.getOccupiedDatesByCarId(id);
                setOccupiedDates(bookedPeriods || []);
                setError(null);
            } catch (err) {
                console.error('Помилка завантаження даних:', err);
                setError('Не вдалося завантажити дані для бронювання.');
            } finally {
                setLoading(false);
            }
        };
        fetchCarAndBookingData();
    }, [id]);

    useEffect(() => {
        const storedUser = localStorage.getItem('user');
        if (!storedUser) return navigate('/login');
        const currentUser = JSON.parse(storedUser);
        if (currentUser.role === 'OWNER') {
            toast.error('Доступ заборонено: власники транспортних засобів не мають права створювати бронювання.');
            navigate('/catalog');
        }
    }, [navigate]);

    useEffect(() => {
        const handleOutsideClick = (e) => {
            if (calendarRef.current && !calendarRef.current.contains(e.target)) {
                setCurrentField(null);
            }
        };
        document.addEventListener('mousedown', handleOutsideClick);
        return () => document.removeEventListener('mousedown', handleOutsideClick);
    }, []);

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

    const isDateOccupied = (checkDate) => {
        const target = new Date(checkDate.getFullYear(), checkDate.getMonth(), checkDate.getDate()).getTime();
        return occupiedDates.some(period => {
            const s = new Date(period.startDate);
            const e = new Date(period.endDate);
            const start = new Date(s.getFullYear(), s.getMonth(), s.getDate()).getTime();
            const end = new Date(e.getFullYear(), e.getMonth(), e.getDate()).getTime();
            return target >= start && target <= end;
        });
    };

    const addCoDriver = () => {
        if (coDrivers.length < 2) {
            setCoDrivers([...coDrivers, { email: '', driverCode: '' }]);
        } else {
            toast.warning('Максимум 2 активні запрошення для спільної оренди за раз!');
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

    const renderCalendarDays = () => {
        const year = viewDate.getFullYear();
        const month = viewDate.getMonth();
        const firstDayIndex = new Date(year, month, 1).getDay();
        const startOffset = firstDayIndex === 0 ? 6 : firstDayIndex - 1;
        const totalDays = new Date(year, month + 1, 0).getDate();
        const daysArray = [];

        for (let i = 0; i < startOffset; i++) {
            daysArray.push(<div key={`empty-${i}`} style={{ padding: '8px' }}></div>);
        }

        for (let day = 1; day <= totalDays; day++) {
            const thisDate = new Date(year, month, day);
            const dateISO = thisDate.toLocaleDateString('en-CA');
            const isPast = dateISO < todayStr;
            const isBeforeStart = currentField === 'end' && dates.start && dateISO <= dates.start;
            const isDisabled = isPast || isBeforeStart || isDateOccupied(thisDate);
            const isSelected = dates.start === dateISO || dates.end === dateISO;

            daysArray.push(
                <div
                    key={`day-${day}`}
                    onClick={() => {
                        if (isDisabled) return;
                        if (currentField === 'start') {
                            setDates({ start: dateISO, end: '' });
                            setCurrentField('end');
                        } else {
                            setDates(prev => ({ ...prev, end: dateISO }));
                            setCurrentField(null);
                        }
                    }}
                    style={{
                        padding: '8px 0', textAlign: 'center', borderRadius: '4px',
                        cursor: isDisabled ? 'not-allowed' : 'pointer',
                        backgroundColor: isSelected ? '#0056b3' : 'transparent',
                        color: isSelected ? '#fff' : (isDisabled ? '#ccc' : '#333'),
                        fontWeight: isSelected ? 'bold' : 'normal',
                        textDecoration: isDisabled && !isPast && !isBeforeStart ? 'line-through' : 'none'
                    }}
                >
                    {day}
                </div>
            );
        }
        return daysArray;
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

            const bookingResult = await bookingService.createBooking(bookingRequest);
            createdBookingId = bookingResult.id;

            try {
                await paymentService.createPayment({
                    bookingId: createdBookingId,
                    amount: days * car.pricePerDay,
                    method: paymentMethod,
                    currency: "USD"
                });
            } catch (paymentErr) {
                if (createdBookingId) await bookingService.cancelBooking(createdBookingId);
                throw new Error(`Оплату не виконано. Бронювання скасовано.`);
            }

            if (coDrivers.length > 0) {
                await Promise.all(coDrivers.map(driver =>
                    bookingService.createInvitation(createdBookingId, {
                        email: driver.email,
                        driverCode: driver.driverCode
                    })
                ));
            }

            toast.success('Бронювання та оплата успішні! 🚗💳');
            setShowPaymentModal(false);
            navigate('/profile');
        } catch (err) {
            toast.error(err.message || "Сталася помилка.");
        } finally {
            setIsProcessing(false);
        }
    };

    if (loading) return <div style={{padding: '100px', textAlign: 'center'}}>Підготовка сторінки... ⏳</div>;
    if (error) return <div style={{padding: '100px', textAlign: 'center', color: 'red'}}>{error}</div>;

    const totalPrice = days * car.pricePerDay;
    const monthsUkr = ["Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень", "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень"];

    return (
        <div className={styles.pageContainer}>
            <h1 className={styles.pageTitle}>Оформлення бронювання</h1>

            <form onSubmit={handleProceedToPayment} className={styles.contentGrid}>
                <div className={styles.leftColumn}>
                    <div className={styles.formSection} style={{ position: 'relative' }}>
                        <h2 className={styles.sectionTitle}>1. Дати оренди</h2>
                        <div className={styles.dateRow}>
                            <div className={styles.inputGroup}>
                                <label>Початок оренди</label>
                                <div onClick={() => setCurrentField('start')} style={{ padding: '10px', border: '1px solid #ccc', borderRadius: '4px', background: '#fff', cursor: 'pointer', minHeight: '40px' }}>
                                    {dates.start ? new Date(dates.start).toLocaleDateString('uk-UA') : 'Оберіть дату...'}
                                </div>
                            </div>
                            <div className={styles.inputGroup}>
                                <label>Завершення оренди</label>
                                <div onClick={() => { if(dates.start) setCurrentField('end') }} style={{ padding: '10px', border: '1px solid #ccc', borderRadius: '4px', background: dates.start ? '#fff' : '#f5f5f5', cursor: dates.start ? 'pointer' : 'not-allowed', minHeight: '40px' }}>
                                    {dates.end ? new Date(dates.end).toLocaleDateString('uk-UA') : 'Оберіть дату...'}
                                </div>
                            </div>
                        </div>

                        {currentField && (
                            <div ref={calendarRef} style={{ position: 'absolute', top: '100%', left: '15px', zIndex: 100, background: '#fff', border: '1px solid #ddd', borderRadius: '8px', padding: '15px', width: '320px', boxShadow: '0 4px 15px rgba(0,0,0,0.15)', marginTop: '5px' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                                    <button type="button" onClick={() => setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() - 1, 1))} style={{ border: 'none', background: 'none', cursor: 'pointer' }}>&lt;</button>
                                    <span style={{ fontWeight: 'bold' }}>{monthsUkr[viewDate.getMonth()]} {viewDate.getFullYear()}</span>
                                    <button type="button" onClick={() => setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 1))} style={{ border: 'none', background: 'none', cursor: 'pointer' }}>&gt;</button>
                                </div>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px', fontSize: '12px', fontWeight: 'bold', color: '#666', textAlign: 'center', marginBottom: '5px' }}>
                                    <div>Пн</div><div>Вт</div><div>Ср</div><div>Чт</div><div>Пт</div><div>Сб</div><div>Нд</div>
                                </div>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px', fontSize: '14px' }}>
                                    {renderCalendarDays()}
                                </div>
                            </div>
                        )}
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
                                    <input type="email" required value={driver.email} onChange={(e) => handleCoDriverChange(index, 'email', e.target.value)} placeholder="friend@carsharing.com"/>
                                </div>
                                <div className={styles.inputGroup}>
                                    <label>Унікальний код водія</label>
                                    <input type="text" required value={driver.driverCode} onChange={(e) => handleCoDriverChange(index, 'driverCode', e.target.value)} placeholder="Напр. RNT5PL91ZX"/>
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
                            <SecureImage src={`/car/v1/${car.id}/images/main`} alt={car.brand} style={{ width: '100%', height: '100%', borderRadius: '4px' }} />
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
                    <div style={{ background: '#fff', padding: '30px', borderRadius: '12px', width: '450px', maxWidth: '90%' }}>
                        <h2 style={{marginTop: 0, marginBottom: '20px', fontSize: '22px'}}>Оплата замовлення</h2>
                        <div style={{ marginBottom: '20px' }}>
                            <label style={{ display: 'flex', alignItems: 'center', padding: '15px', border: paymentMethod === 'CARD' ? '2px solid #0056b3' : '1px solid #ddd', borderRadius: '8px', marginBottom: '10px', cursor: 'pointer', background: paymentMethod === 'CARD' ? '#f8fbff' : '#fff' }}>
                                <input type="radio" name="payMethod" checked={paymentMethod === 'CARD'} onChange={() => setPaymentMethod('CARD')} style={{marginRight: '15px'}} />
                                💳 Банківська картка
                            </label>
                            {paymentMethod === 'CARD' && (
                                <div style={{ padding: '0 10px 15px 35px' }}>
                                    <input type="text" placeholder="Номер картки" style={{ width: '100%', padding: '10px', marginBottom: '10px', border: '1px solid #ccc', borderRadius: '4px' }}/>
                                    <div style={{ display: 'flex', gap: '10px' }}>
                                        <input type="text" placeholder="MM/YY" style={{ width: '50%', padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}/>
                                        <input type="text" placeholder="CVC" style={{ width: '50%', padding: '10px', border: '1px solid #ccc', borderRadius: '4px' }}/>
                                    </div>
                                </div>
                            )}

                            {/* 👑 КРИТИЧНИЙ ФІКС: Повернули Google Pay та Apple Pay */}
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
                            <button onClick={() => setShowPaymentModal(false)} style={{ padding: '12px 20px', border: 'none', background: '#eee', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}>Скасувати</button>
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
