import React, { useState, useEffect } from 'react';
import { documentService } from '../../services/document.service';
import { toast } from 'react-toastify';
import SecureImage from '../../components/SecureImage';

const KycManagement = () => {
    const [searchUserId, setSearchUserId] = useState('');
    const [userDocs, setUserDocs] = useState([]);
    const [isProfileReady, setIsProfileReady] = useState(false);
    const [loading, setLoading] = useState(true);

    // 👑 Стейт для модального вікна детального перегляду
    const [selectedDocForModal, setSelectedDocForModal] = useState(null);
    const [isQueueMode, setIsQueueMode] = useState(true);

    const fetchUnverifiedQueue = async () => {
        try {
            setLoading(true);
            setSelectedDocForModal(null);
            setIsQueueMode(true);
            setSearchUserId('');
            const data = await documentService.getUnverifiedDocuments();
            setUserDocs(data || []);
        } catch (err) {
            console.error("Помилка завантаження черги KYC:", err);
            toast.error("Не вдалося завантажити чергу документів.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUnverifiedQueue();
    }, []);

    const handleInspectUser = async (e) => {
        e.preventDefault();
        if (!searchUserId.trim()) return fetchUnverifiedQueue();

        try {
            setLoading(true);
            setSelectedDocForModal(null);
            setIsQueueMode(false);

            const meta = await documentService.getMetadata(searchUserId.trim());
            setUserDocs(meta || []);

            const status = await documentService.getProfileStatus(searchUserId.trim());
            setIsProfileReady(status);
        } catch (err) {
            if (err.response?.status === 404) {
                setUserDocs([]);
                setIsProfileReady(false);
                toast.info("Користувач із таким ID не знайдений або не має документів.");
            } else {
                toast.error("Помилка завантаження профілю.");
            }
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyDoc = async (docId) => {
        try {
            await documentService.verifyDocument(docId);
            toast.success("Документ успішно верифіковано! ✅");

            // Якщо вікно деталей відчинене — закриваємо його
            setSelectedDocForModal(null);

            if (isQueueMode) {
                setUserDocs(prev => prev.filter(d => d.id !== docId));
            } else {
                setUserDocs(prev => prev.map(d => d.id === docId ? { ...d, isVerified: true } : d));
                const status = await documentService.getProfileStatus(searchUserId.trim());
                setIsProfileReady(status);
            }
        } catch (err) {
            toast.error("Не вдалося верифікувати документ.");
        }
    };

    return (
        <div style={{ padding: '24px', fontFamily: '"Segoe UI", Roboto, sans-serif', backgroundColor: '#f8fafc', minHeight: '100vh' }}>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h1 style={{ fontSize: '24px', fontWeight: '700', color: '#0f172a', margin: 0 }}>
                    {isQueueMode ? '📥 Черга державної перевірки документів (KYC)' : `🔍 Документи користувача ID #${searchUserId}`}
                </h1>
                {!isQueueMode && (
                    <button onClick={fetchUnverifiedQueue} style={{ padding: '8px 16px', background: '#fff', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '6px', fontWeight: '600', cursor: 'pointer', fontSize: '13px' }}>
                        ↩ Повернутися до черги
                    </button>
                )}
            </div>

            {/* Панель пошуку */}
            <form onSubmit={handleInspectUser} style={{ display: 'flex', gap: '12px', marginBottom: '24px', background: '#fff', padding: '16px', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 1px 3px rgba(0,0,0,0.02)' }}>
                <input
                    type="number"
                    placeholder="Введіть ID користувача для точкової перевірки профілю..."
                    value={searchUserId}
                    onChange={(e) => setSearchUserId(e.target.value)}
                    style={{ flex: 1, padding: '12px 16px', borderRadius: '8px', border: '1px solid #cbd5e1', fontSize: '14px', outline: 'none' }}
                />
                <button type="submit" style={{ padding: '12px 24px', background: '#0056b3', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', fontSize: '14px' }}>
                    Знайти 🔍
                </button>
            </form>

            {loading ? (
                <div style={{ textAlign: 'center', padding: '60px', fontSize: '15px', color: '#64748b' }}>Завантаження даних черги модерації... ⏳</div>
            ) : userDocs.length > 0 ? (
                <div style={{ background: '#fff', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.03)', overflow: 'hidden' }}>

                    {!isQueueMode && (
                        <div style={{ padding: '14px 20px', background: isProfileReady ? '#f0fdf4' : '#fff7ed', borderBottom: '1px solid #e2e8f0', color: isProfileReady ? '#166534' : '#c2410c', fontSize: '14px', fontWeight: '600' }}>
                            Статус водія: {isProfileReady ? '🟢 ПОВНІСТЮ ВЕРИФІКОВАНИЙ' : '🟡 БЛОКОВАНИЙ (Є неперевірені файли)'}
                        </div>
                    )}

                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
                        <thead>
                            <tr style={{ background: '#f8fafc', borderBottom: '1px solid #e2e8f0', color: '#64748b', fontWeight: '600' }}>
                                <th style={{ padding: '16px' }}>Користувач</th>
                                <th style={{ padding: '16px' }}>Тип документа</th>
                                <th style={{ padding: '16px' }}>Назва файлу</th>
                                <th style={{ padding: '16px', textAlign: 'center' }}>Статус</th>
                                <th style={{ padding: '16px', textAlign: 'center' }}>Дії</th>
                            </tr>
                        </thead>
                        <tbody>
                            {userDocs.map(doc => (
                                <tr key={doc.id} style={{ borderBottom: '1px solid #f1f5f9', transition: 'background 0.2s' }} className="table-row-hover">
                                    <td style={{ padding: '16px' }}>
                                        <span style={{ background: '#f1f5f9', color: '#1e293b', padding: '6px 10px', borderRadius: '6px', fontWeight: '700', fontFamily: 'monospace' }}>
                                            👤 ID #{doc.userId || '—'}
                                        </span>
                                    </td>
                                    <td style={{ padding: '16px', fontWeight: '600', color: '#1e293b' }}>{doc.documentType}</td>
                                    <td style={{ padding: '16px', color: '#475569', fontSize: '13px' }}>{doc.originalFileName}</td>
                                    <td style={{ padding: '16px', textAlign: 'center' }}>
                                        <span style={{ fontSize: '11px', fontWeight: '700', padding: '4px 10px', borderRadius: '6px', backgroundColor: doc.isVerified ? '#dcfce7' : '#fef3c7', color: doc.isVerified ? '#15803d' : '#b45309' }}>
                                            {doc.isVerified ? 'VERIFIED' : 'PENDING'}
                                        </span>
                                    </td>
                                    <td style={{ padding: '16px', textAlign: 'center' }}>
                                        <div style={{ display: 'flex', gap: '8px', justifyContent: 'center' }}>
                                            {/* 👑 КНОПКА ДЕТАЛІ ДЛЯ РОЗГОРТАННЯ МОДАЛКИ */}
                                            <button
                                                onClick={() => setSelectedDocForModal(doc)}
                                                style={{ padding: '6px 14px', background: '#0056b3', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: '600' }}
                                            >
                                                👁️ Деталі
                                            </button>
                                            {!doc.isVerified && (
                                                <button
                                                    onClick={() => handleVerifyDoc(doc.id)}
                                                    style={{ padding: '6px 14px', background: '#22c55e', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: '600' }}
                                                >
                                                    Схвалити
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            ) : (
                <div style={{ background: '#fff', padding: '40px', borderRadius: '12px', border: '1px solid #e2e8f0', textAlign: 'center', color: '#64748b', boxShadow: '0 1px 3px rgba(0,0,0,0.02)' }}>
                    🎉 <strong>Черга порожня!</strong> Усі документи успішно перевірені модератором.
                </div>
            )}

            {/* 👑 ПОДВІЙНЕ ОНОВЛЕННЯ: МОДАЛЬНЕ ВІКНО НА ПОВНИЙ ЕКРАН ДЛЯ ПЕРЕГЛЯДУ ДОКУМЕНТА */}
            {selectedDocForModal && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(15, 23, 42, 0.6)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 2000, backdropFilter: 'blur(4px)' }} onClick={() => setSelectedDocForModal(null)}>
                    <div style={{ background: '#fff', borderRadius: '16px', width: '600px', maxWidth: '95%', padding: '24px', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)', display: 'flex', flexDirection: 'column', gap: '16px' }} onClick={e => e.stopPropagation()}>

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f1f5f9', paddingBottom: '12px' }}>
                            <h3 style={{ margin: 0, fontSize: '18px', fontWeight: '700', color: '#0f172a' }}>📄 Перевірка документа #DOC-{selectedDocForModal.id}</h3>
                            <button onClick={() => setSelectedDocForModal(null)} style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: '#94a3b8' }}>✕</button>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', fontSize: '13px', color: '#475569', background: '#f8fafc', padding: '12px', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
                            <div>👤 <strong>Користувач:</strong> ID #{selectedDocForModal.userId || '—'}</div>
                            <div>📁 <strong>Категорія:</strong> {selectedDocForModal.documentType}</div>
                            <div style={{ gridColumn: '1 / span 2' }}>📝 <strong>Назва файлу:</strong> {selectedDocForModal.originalFileName}</div>
                        </div>

                        <div style={{ border: '1px dashed #cbd5e1', borderRadius: '8px', overflow: 'hidden', background: '#fafafa', display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '350px', maxHeight: '500px' }}>
                            {selectedDocForModal.contentType?.includes('image') ? (
                                <SecureImage
                                    src={`/document/v1/${selectedDocForModal.id}/download`}
                                    alt="KYC full resolution preview"
                                    style={{ maxWidth: '100%', maxHeight: '480px', objectFit: 'contain' }}
                                />
                            ) : (
                                <div style={{ textAlign: 'center', padding: '20px' }}>
                                    <div style={{ fontSize: '50px' }}>📄</div>
                                    <div style={{ fontSize: '14px', color: '#0f172a', fontWeight: '600', marginTop: '5px' }}>Документ у форматі PDF</div>
                                    <a
                                        href={`http://localhost:8100/document/v1/${selectedDocForModal.id}/download`}
                                        target="_blank"
                                        rel="noreferrer"
                                        style={{ display: 'inline-block', marginTop: '12px', padding: '8px 16px', background: '#0056b3', color: '#fff', borderRadius: '6px', textDecoration: 'none', fontSize: '13px', fontWeight: '600' }}
                                    >
                                        Відкрити в новій вкладці ↗
                                    </a>
                                </div>
                            )}
                        </div>

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', borderTop: '1px solid #f1f5f9', paddingTop: '16px', marginTop: '4px' }}>
                            <button onClick={() => setSelectedDocForModal(null)} style={{ padding: '10px 18px', background: '#f1f5f9', color: '#475569', border: '1px solid #cbd5e1', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', fontSize: '13px' }}>
                                Закрити
                            </button>
                            {!selectedDocForModal.isVerified && (
                                <button onClick={() => handleVerifyDoc(selectedDocForModal.id)} style={{ padding: '10px 22px', background: '#22c55e', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer', fontSize: '13px' }}>
                                    Затвердити документ дні ✅
                                </button>
                            )}
                        </div>

                    </div>
                </div>
            )}
        </div>
    );
};

export default KycManagement;
