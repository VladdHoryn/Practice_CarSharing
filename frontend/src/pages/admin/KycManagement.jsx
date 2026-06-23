import React, { useState, useEffect } from 'react';
import { documentService } from '../../services/document.service';
import { toast } from 'react-toastify';
import SecureImage from '../../components/SecureImage';
import styles from './KycManagement.module.css';

const KycManagement = () => {
    const [searchUserId, setSearchUserId] = useState('');
    const [userDocs, setUserDocs] = useState([]);
    const [isProfileReady, setIsProfileReady] = useState(false);
    const [loading, setLoading] = useState(true);
    const [activeInspectorDoc, setActiveInspectorDoc] = useState(null);

    // 👑 Режим роботи: true — загальна черга неперевірених, false — перегляд конкретного юзера
    const [isQueueMode, setIsQueueMode] = useState(true);

    // 👑 Завантаження загальної черги неперевірених документів
    const fetchUnverifiedQueue = async () => {
        try {
            setLoading(true);
            setActiveInspectorDoc(null);
            setIsQueueMode(true);
            setSearchUserId(''); // Очищаємо інпут

            const data = await documentService.getUnverifiedDocuments();
            setUserDocs(data || []);
        } catch (err) {
            console.error("Помилка завантаження черги неперевірених:", err);
            toast.error("Не вдалося завантажити загальну чергу документів.");
        } finally {
            setLoading(false);
        }
    };

    // Автоматично стягуємо дані при відкритті вкладки
    useEffect(() => {
        fetchUnverifiedQueue();
    }, []);

    // Точковий пошук документів по конкретному UserId
    const handleInspectUser = async (e) => {
        e.preventDefault();
        if (!searchUserId.trim()) return fetchUnverifiedQueue();

        try {
            setLoading(true);
            setActiveInspectorDoc(null);
            setIsQueueMode(false);

            const meta = await documentService.getMetadata(searchUserId.trim());
            setUserDocs(meta || []);

            const status = await documentService.getProfileStatus(searchUserId.trim());
            setIsProfileReady(status);
        } catch (err) {
            if (err.response?.status === 404) {
                setUserDocs([]);
                setIsProfileReady(false);
                toast.info("Цей користувач не завантажував документи або ID невірний.");
            } else {
                console.error(err);
                toast.error("Помилка завантаження даних користувача.");
            }
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyDoc = async (docId) => {
        try {
            await documentService.verifyDocument(docId);
            toast.success("Документ успішно затверджено! ✅");

            if (isQueueMode) {
                // 👑 У загальній черзі просто викидаємо документ зі списку (він же став верифікованим)
                setUserDocs(prev => prev.filter(d => d.id !== docId));
                setActiveInspectorDoc(null);
            } else {
                // В режимі інспекції юзера міняємо статус на true локально
                setUserDocs(prev => prev.map(d => d.id === docId ? { ...d, isVerified: true } : d));

                // Перераховуємо готовність всього профілю
                const status = await documentService.getProfileStatus(searchUserId.trim());
                setIsProfileReady(status);
            }
        } catch (err) {
            toast.error("Не вдалося верифікувати документ.");
        }
    };

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>
                {isQueueMode ? '📥 Загальна черга верифікації документів (KYC)' : `🔍 Інспекція документів користувача #${searchUserId}`}
            </h1>

            {/* Форма пошуку + Скидання черги */}
            <form onSubmit={handleInspectUser} style={{ display: 'flex', gap: '10px', marginBottom: '25px', background: '#fff', padding: '15px', borderRadius: '8px', border: '1px solid #eee' }}>
                <input
                    type="number"
                    placeholder="Пошук документів за ID користувача..."
                    value={searchUserId}
                    onChange={(e) => setSearchUserId(e.target.value)}
                    style={{ flex: 1, padding: '10px', borderRadius: '4px', border: '1px solid #ccc', fontSize: '14px' }}
                />
                <button type="submit" style={{ padding: '10px 20px', background: '#0056b3', color: '#fff', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                    Шукати 🔍
                </button>
                {!isQueueMode && (
                    <button type="button" onClick={fetchUnverifiedQueue} style={{ padding: '10px 20px', background: '#6c757d', color: '#fff', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                        Назад до всієї черги ↩
                    </button>
                )}
            </form>

            {loading ? (
                <div className={styles.loader} style={{ textAlign: 'center', padding: '30px' }}>Обробка черги документів... ⏳</div>
            ) : userDocs.length > 0 ? (
                <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '30px' }}>

                    {/* Ліва частина: Таблиця документів */}
                    <div>
                        {!isQueueMode && (
                            <div style={{ padding: '10px 15px', borderRadius: '6px', marginBottom: '15px', background: isProfileReady ? '#e6f7ff' : '#fff7e6', border: isProfileReady ? '1px solid #91d5ff' : '1px solid #ffd591', fontSize: '14px', fontWeight: 'bold' }}>
                                Status: {isProfileReady ? '🟢 ГОТОВИЙ (Всі 3 типи документів схвалено)' : '🟡 БЛОКОВАНИЙ'}
                            </div>
                        )}

                        <table className={styles.table} style={{ width: '100%', background: '#fff', borderRadius: '8px', borderCollapse: 'collapse', border: '1px solid #eee' }}>
                            <thead>
                                <tr style={{ background: '#f8f9fa', borderBottom: '2px solid #eee' }}>
                                    <th style={{ padding: '12px', textAlign: 'left' }}>ID Док.</th>
                                    <th style={{ padding: '12px', textAlign: 'left' }}>Тип документа</th>
                                    <th style={{ padding: '12px', textAlign: 'left' }}>Назва файлу</th>
                                    <th style={{ padding: '12px', textAlign: 'center' }}>Статус</th>
                                    <th style={{ padding: '12px', textAlign: 'center' }}>Дії</th>
                                </tr>
                            </thead>
                            <tbody>
                                {userDocs.map(doc => (
                                    <tr key={doc.id} style={{ borderBottom: '1px solid #eee', cursor: 'pointer', background: activeInspectorDoc?.id === doc.id ? '#f4f7fe' : 'transparent' }} onClick={() => setActiveInspectorDoc(doc)}>
                                        <td style={{ padding: '12px', fontSize: '13px', color: '#666' }}>#DOC-{doc.id}</td>
                                        <td style={{ padding: '12px' }}><strong>{doc.documentType}</strong></td>
                                        <td style={{ padding: '12px', fontSize: '13px', color: '#555' }}>{doc.originalFileName}</td>
                                        <td style={{ padding: '12px', textAlign: 'center' }}>
                                            <span style={{ fontSize: '11px', fontWeight: 'bold', padding: '3px 8px', borderRadius: '4px', backgroundColor: doc.isVerified ? '#d4edda' : '#fff3cd', color: doc.isVerified ? '#155724' : '#856404' }}>
                                                {doc.isVerified ? 'VERIFIED' : 'PENDING'}
                                            </span>
                                        </td>
                                        <td style={{ padding: '12px', textAlign: 'center' }} onClick={e => e.stopPropagation()}>
                                            {!doc.isVerified ? (
                                                <button onClick={() => handleVerifyDoc(doc.id)} style={{ padding: '4px 10px', background: '#28a745', color: '#fff', border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }}>
                                                    Схвалити ✅
                                                </button>
                                            ) : (
                                                <span style={{ fontSize: '12px', color: '#aaa', fontStyle: 'italic' }}>Затверджено</span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    {/* Права частина: Інспектор файлів */}
                    <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', border: '1px solid #eee', display: 'flex', flexDirection: 'column' }}>
                        <h3 style={{ margin: '0 0 15px 0', fontSize: '16px' }}>🖼️ Перегляд вмісту документа</h3>
                        {activeInspectorDoc ? (
                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '15px' }}>
                                <div style={{ fontSize: '13px', color: '#4a5568', background: '#f8f9fa', padding: '10px', borderRadius: '4px' }}>
                                    <strong>ID запису:</strong> {activeInspectorDoc.id} <br/>
                                    <strong>Тип:</strong> {activeInspectorDoc.documentType} <br/>
                                    <strong>Формат:</strong> {activeInspectorDoc.contentType}
                                </div>

                                <div style={{ flex: 1, border: '1px dashed #ccc', borderRadius: '6px', overflow: 'hidden', minHeight: '300px', background: '#fafafa', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                    {activeInspectorDoc.contentType?.includes('image') ? (
                                        <SecureImage
                                            src={`/document/v1/${activeInspectorDoc.id}/download`}
                                            alt="KYC Document content"
                                            style={{ maxWidth: '100%', maxHeight: '400px', objectFit: 'contain' }}
                                        />
                                    ) : (
                                        <div style={{ textAlign: 'center', padding: '20px' }}>
                                            <div style={{ fontSize: '40px' }}>📄</div>
                                            <div style={{ fontSize: '13px', marginTop: '5px' }}>Формат документа: <strong>PDF</strong>.</div>
                                            <a
                                                href={`http://localhost:8100/document/v1/${activeInspectorDoc.id}/download`}
                                                target="_blank"
                                                rel="noreferrer"
                                                style={{ display: 'inline-block', marginTop: '10px', padding: '6px 12px', background: '#0056b3', color: '#fff', borderRadius: '4px', textDecoration: 'none', fontSize: '12px', fontWeight: 'bold' }}
                                            >
                                                Відкрити PDF у новій вкладці ↗
                                            </a>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ) : (
                            <div style={{ color: '#888', fontStyle: 'italic', paddingTop: '80px', textAlign: 'center' }}>
                                👈 Оберіть документ із лівої таблиці черги для активації вікна інспектора.
                            </div>
                        )}
                    </div>

                </div>
            ) : (
                <div style={{ background: '#fff', padding: '40px', borderRadius: '8px', border: '1px solid #eee', textAlign: 'center', color: '#666' }}>
                    🎉 <strong>Всі документи перевірено!</strong> Черга модерації пуста, нових заявок немає.
                </div>
            )}
        </div>
    );
};

export default KycManagement;
