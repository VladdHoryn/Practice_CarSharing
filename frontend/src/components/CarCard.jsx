import React from 'react';

const CarCard = ({ car }) => {
    // Якщо немає фото – використовуємо заглушку
    const fallbackImage = '/images/car-placeholder.png';
    const imageUrl = car.imageUrl || car.primaryImage || 
                     (car.images && car.images.length > 0 ? car.images[0] : fallbackImage);

    return (
        <div className="car-card">
            <img 
                src={imageUrl} 
                alt={`${car.brand} ${car.model}`}
                onError={(e) => {
                    e.target.src = fallbackImage;
                }}
            />
            <h3>{car.brand} {car.model}</h3>
            <p>{car.year} - {car.carClass}</p>
            <p>Ціна: {car.pricePerDay} €/день</p>
            <p>Статус: {car.status}</p>
        </div>
    );
};

export default CarCard;
