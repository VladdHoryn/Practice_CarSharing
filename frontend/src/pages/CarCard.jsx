import React, { useState } from 'react';
import styles from './CarCard.module.css';

const CarCard = ({ car, onDetailClick }) => {
    const [imageLoading, setImageLoading] = useState(true);

    const mainImageSrc = `http://localhost:8100/car/v1/${car.id}/images/main`;

    const placeholderSvg = `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="400" height="250" viewBox="0 0 400 250" style="background:%23eee;"><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="16" fill="%23666">🚗 Фото тимчасово відсутнє</text></svg>`;

    return (
        <div className={styles.card} onClick={() => onDetailClick(car.id)}>
            <div className={styles.imageWrapper}>
                {imageLoading && <div className={styles.skeleton}></div>}

                <img
                    src={mainImageSrc}
                    alt={`${car.brand} ${car.model}`}
                    loading="lazy"
                    onLoad={() => setImageLoading(false)}
                    onError={(e) => {
                        setImageLoading(false);
                        e.target.src = placeholderSvg;
                    }}
                    className={`${styles.carImage} ${imageLoading ? styles.hidden : ''}`}
                />
            </div>

            <div className={styles.cardContent}>
                <div className={styles.classBadge}>{car.carClass}</div>
                <h3 className={styles.carTitle}>{car.brand} {car.model}</h3>
                <div className={styles.cardFooter}>
                    <span className={styles.price}><strong>{car.pricePerDay}€</strong> / доба</span>
                    <span className={styles.status} style={{ color: car.status === 'AVAILABLE' ? '#28a745' : '#f39c12' }}>
                        ● {car.status}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default CarCard;
