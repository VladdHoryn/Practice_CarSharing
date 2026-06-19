import React, { useState, useEffect } from 'react';
import { carService } from '../../services/car.service';
import styles from './CarGallery.module.css';

const CarGallery = ({ carId }) => {
    const [images, setImages] = useState([]);
    const [activeImageId, setActiveImageId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [mainLoading, setMainLoading] = useState(true);

    const placeholderSvg = `data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400" viewBox="0 0 600 400" style="background:%23eee;"><text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle" font-family="sans-serif" font-size="18" fill="%23666">🚗 Галерея авто порожня</text></svg>`;

    useEffect(() => {
        if (!carId) return;

        const loadGalleryMetadata = async () => {
            try {
                setLoading(true);
                const metadata = await carService.getCarImages(carId);
                setImages(metadata || []);

                const mainImg = metadata.find(img => img.isMain) || metadata[0];
                if (mainImg) {
                    setActiveImageId(mainImg.id);
                }
            } catch (err) {
                console.error("Помилка завантаження галереї:", err);
            } finally {
                setLoading(false);
            }
        };

        loadGalleryMetadata();
    }, [carId]);

    const handleThumbnailClick = (id) => {
        setMainLoading(true);
        setActiveImageId(id);
    };

    if (loading) {
        return (
            <div className={styles.galleryContainer}>
                <div className={`${styles.mainSkeleton} ${styles.pulse}`}></div>
                <div className={styles.thumbnailsSkeletonGrid}>
                    <div className={`${styles.thumbSkeleton} ${styles.pulse}`}></div>
                    <div className={`${styles.thumbSkeleton} ${styles.pulse}`}></div>
                    <div className={`${styles.thumbSkeleton} ${styles.pulse}`}></div>
                </div>
            </div>
        );
    }

    if (images.length === 0) {
        return (
            <div className={styles.galleryContainer}>
                <img src={placeholderSvg} alt="No media available" className={styles.mainImage} />
            </div>
        );
    }

    return (
        <div className={styles.galleryContainer}>
            <div className={styles.mainImageWrapper}>
                {mainLoading && <div className={styles.mainSkeleton}></div>}
                <img
                    src={`http://localhost:8100/car/v1/${carId}/images/${activeImageId}`}
                    alt="Active vehicle view"
                    onLoad={() => setMainLoading(false)}
                    onError={(e) => {
                        setMainLoading(false);
                        e.target.src = placeholderSvg;
                    }}
                    className={`${styles.mainImage} ${mainLoading ? styles.hidden : ''}`}
                />
            </div>

            <div className={styles.thumbnailsWrapper}>
                {images.map((img) => (
                    <div
                        key={img.id}
                        className={`${styles.thumbCard} ${img.id === activeImageId ? styles.activeThumb : ''}`}
                        onClick={() => handleThumbnailClick(img.id)}
                    >
                        <img
                            src={`http://localhost:8100/car/v1/${carId}/images/${img.id}`}
                            alt="Vehicle thumbnail"
                            loading="lazy"
                            className={styles.thumbnailImage}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
};

export default CarGallery;
