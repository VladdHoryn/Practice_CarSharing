import React, { useState, useEffect } from 'react';
import { userService } from '../../services/user.service';
import { toast } from 'react-toastify';
import styles from './KycManagement.module.css';

const KycManagement = () => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    // Стейты для Lightbox (Перегляд документів)
    const [activeImage, setActiveImage] = useState(null);
    const [zoom, setZoom] = useState(1);
    const [rotation, setRotation] = useState(0);

    // Стейты для Модалки відхилення (Reject)
    const [isRejectModalOpen, setIsRejectModalOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [rejectReason, setRejectReason] = useState('');

    const fetchKycQueue = async () => {
        try {
            setLoading(true);
            // На майбутнє: const data = await userService.getPendingKyc();
            // setRequests(data);
            throw new Error("API not ready");
        } catch (err) {
            // Мокові дані для повноцінного тестування черги KYC та Lightbox
            setRequests([
                {
                    id: 105,
                    fullName: 'Максим Журик',
                    email: 'maks.zh@gmail.com',
                    status: 'PENDING_VERIFICATION',
                    documentType: 'Посвідчення водія (Категорія B)',
                    docFrontUrl: 'https://images.unsplash.com/photo-1554415707-6e8cfc93fe23?q=80&w=600', // Тимчасові фото-заглушки документів
                    docBackUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?q=80&w=600'
                },
                {
                    id: 106,
                    fullName: 'Дмитро Чернівецький',
                    email: 'dimon.cv@ukr.net',
                    status: 'PENDING_VERIFICATION',
                    documentType: 'ID-Картка / Паспорт',
                    docFrontUrl: 'https://images.unsplash.com/photo-1568602471122-7832951cc4c5?q=80&w=600',
                    docBackUrl: ''
                }
            ]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchKycQueue();
    }, []);

    const handleApprove = async (userId) => {
        try {
            // На майбутнє: await userService.approveKyc(userId);
            toast.success('Користувача успішно верифіковано! Доступ до оренди відкрито. 🎉');
            setRequests(prev => prev.filter(r => r.id !== userId));
        } catch (err) {
            toast.error('Помилка при затвердженні запиту.');
        }
    };

    const openRejectModal = (user) => {
        setSelectedUser(user);
        setRejectReason('');
        setIsRejectModalOpen(true);
    };

    const handleRejectSubmit = async (e) => {
        e.preventDefault();
        if (!rejectReason.trim()) {
            toast.warning('Причина відмови обовʼязкова для заповнення!');
            return;
        }

        try {
            // На майбутнє: await userService.rejectKyc(selectedUser.id, rejectReason);
            toast.info(`Заявку користувача ${selectedUser.fullName} відхилено. Причина: ${rejectReason}`);
            setIsRejectModalOpen(false);
            setRequests(prev => prev.filter(r => r.id !== selectedUser.id));
        } catch (err) {
            toast.error('Не вдалося надіслати статус відмови.');
        }
    };

    // Керування Lightbox
    const openLightbox = (url) => {
        setActiveImage(url);
        setZoom(1);
        setRotation(0);
    };

    const closeLightbox = () => {
        setActiveImage(null);
    };

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>🛡️ Черга верифікації користувачів (KYC)</h1>

            {loading ? (
                <div className={styles.loader}>Завантаження скан-копій документів... ⏳</div>
            ) : requests.length === 0 ? (
                <div className={styles.emptyState}>Усі документи перевірено. Черга пуста! ✅</div>
            ) : (
                <div className={styles.queueGrid}>
                    {requests.map((r) => (
                        <div key={r.id} className={styles.kycCard}>
                            <div className={styles.cardHeader}>
                                <div>
                                    <h3>{r.fullName}</h3>
                                    <p>{r.email} (ID: #{r.id})</p>
                                </div>
                                <span className={styles.badge}>PENDING</span>
                            </div>

                            <div className={styles.docSection}>
                                <strong>📄 {r.documentType}</strong>
                                <div className={styles.imageContainer}>
                                    {r.docFrontUrl && (
                                        <div className={styles.thumbBox} onClick={() => openLightbox(r.docFrontUrl)}>
                                            <img src={r.docFrontUrl} alt="Лицьова сторона" />
                                            <span>🔎 Сторона 1</span>
                                        </div>
                                    )}
                                    {r.docBackUrl && (
                                        <div className={styles.thumbBox} onClick={() => openLightbox(r.docBackUrl)}>
                                            <img src={r.docBackUrl} alt="Зворотна сторона" />
                                            <span>🔎 Сторона 2</span>
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div className={styles.cardActions}>
                                <button onClick={() => openRejectModal(r)} className={styles.rejectBtn}>Відхилити</button>
                                <button onClick={() => handleApprove(r.id)} className={styles.approveBtn}>Затвердити</button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* 🖼 LIGHTBOX (МОДАЛКА ПЕРЕГЛЯДУ З ЗУМОМ ТА ПОВОРОТОМ) */}
            {activeImage && (
                <div className={styles.lightboxOverlay} onClick={closeLightbox}>
                    <div className={styles.lightboxContainer} onClick={(e) => e.stopPropagation()}>

                        {/* Панель інструментів модератора */}
                        <div className={styles.toolbar}>
                            <button onClick={() => setZoom(prev => Math.min(prev + 0.3, 3))} title="Збільшить">➕ Наблизити</button>
                            <button onClick={() => setZoom(prev => Math.max(prev - 0.3, 0.6))} title="Зменшити">➖ Віддалити</button>
                            <button onClick={() => setRotation(prev => prev + 90)} title="Повернути">🔄 Повернути на 90°</button>
                            <button onClick={closeLightbox} className={styles.closeToolbarBtn}>Закрити ✖</button>
                        </div>

                        {/* Область перегляду зображення */}
                        <div className={styles.imageWrapper}>
                            <img
                                src={activeImage}
                                alt="Документ у високій роздільній здатності"
                                style={{
                                    transform: `scale(${zoom}) rotate(${rotation}deg)`,
                                    transition: 'transform 0.2s ease-out'
                                }}
                            />
                        </div>
                    </div>
                </div>
            )}

            {/* 📝 ОБО'В'ЯЗКОВА МОДАЛКА ВІДХИЛЕННЯ З ВАЛІДАЦІЄЮ ПРИЧИНИ */}
            {isRejectModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <h3>❌ Відмова у верифікації</h3>
                        <p>Вкажіть офіційну причину відмови для користувача <strong>{selectedUser?.fullName}</strong>. Цей текст буде надіслано йому на пошту:</p>

                        <form onSubmit={handleRejectSubmit} className={styles.rejectForm}>
                            <textarea
                                required
                                rows="4"
                                placeholder="Наприклад: Скан-копія водійського посвідчення розмита або закінчився термін дії документа..."
                                className={styles.textarea}
                                value={rejectReason}
                                onChange={(e) => setRejectReason(e.target.value)}
                            />
                            <div className={styles.modalActions}>
                                <button type="button" onClick={() => setIsRejectModalOpen(false)} className={styles.cancelBtn}>Скасувати</button>
                                <button type="submit" className={styles.confirmRejectBtn}>Надіслати відмову</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default KycManagement;
