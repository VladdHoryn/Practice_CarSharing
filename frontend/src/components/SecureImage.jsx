import React, { useState, useEffect } from 'react';
import apiClient from '../api/apiClient';

const SecureImage = ({ src, alt, className, style }) => {
    const mockImages = [
        "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=600&q=80", // Porsche
        "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?auto=format&fit=crop&w=600&q=80", // Chevrolet
        "https://images.unsplash.com/photo-1494976388531-d1058494cdd8?auto=format&fit=crop&w=600&q=80", // Mustang
        "https://images.unsplash.com/photo-1580273916550-e323be2ae537?auto=format&fit=crop&w=600&q=80", // BMW
        "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=600&q=80"  // Toyota
    ];

    const getDeterministicMock = (path) => {
        if (!path) return mockImages[0];
        const idMatch = path.match(/\d+/);
        const index = idMatch ? parseInt(idMatch[0], 10) % mockImages.length : 0;
        return mockImages[index];
    };

    const [imgUrl, setImgUrl] = useState(mockImages[0]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!src) return;

        // 👑 ПЕРЕВІРКА АВТОРИЗАЦІЇ ДЛЯ ГОСТЕЙ (ЗАХИСТ ВІД РЕДІРЕКТУ НА ЛОГІН)
        const storedUser = localStorage.getItem('user');
        if (!storedUser) {
            // Якщо токена/користувача немає — ми в режимі гостя.
            // Відразу ставимо гарне демо-фото і відключаємо лоадер, не турбуючи бекенд.
            setImgUrl(getDeterministicMock(src));
            setLoading(false);
            return;
        }

        let localBlobUrl = null;

        const loadBinaryImage = async () => {
            try {
                setLoading(true);
                const response = await apiClient.get(src, { responseType: 'blob' });
                localBlobUrl = URL.createObjectURL(response.data);
                setImgUrl(localBlobUrl);
            } catch (err) {
                console.warn(`Фото не знайдено на сервері (${src}), вмикаємо візуальний демо-режим.`);
                setImgUrl(getDeterministicMock(src));
            } finally {
                setLoading(false);
            }
        };

        loadBinaryImage();

        return () => {
            if (localBlobUrl) {
                URL.revokeObjectURL(localBlobUrl);
            }
        };
    }, [src]);

    return (
        <div style={{ width: '100%', height: '100%', position: 'relative', ...style }} className={className}>
            {loading && (
                <div style={{
                    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                    background: 'linear-gradient(90deg, #f2f2f2 25%, #e6e6e6 50%, #f2f2f2 75%)',
                    backgroundSize: '200% 100%', animation: 'shimmer 1.5s infinite linear',
                    borderRadius: style?.borderRadius || '0'
                }}></div>
            )}
            <img
                src={imgUrl}
                alt={alt}
                style={{ width: '100%', height: '100%', objectFit: 'cover', display: loading ? 'none' : 'block', borderRadius: style?.borderRadius || '0' }}
            />
            <style>{`
                @keyframes shimmer {
                    0% { background-position: 200% 0; }
                    100% { background-position: -200% 0; }
                }
            `}</style>
        </div>
    );
};

export default SecureImage;
