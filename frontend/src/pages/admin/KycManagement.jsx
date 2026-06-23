import React, { useState } from 'react';
import { documentService } from '../../services/document.service';
import { toast } from 'react-toastify';
import SecureImage from '../../components/SecureImage';
import styles from './KycManagement.module.css';

const KycManagement = () => {
    const [searchUserId, setSearchUserId] = useState('');
    const [userDocs, setUserDocs] = useState([]);
    const [isProfileReady, setIsProfileReady] = useState(false);
    const [loading, setLoading] = useState(false);
    const [activeInspectorDoc, setActiveInspectorDoc] = useState(null);

    const handleInspectUser = async (e) => {
            e.preventDefault();
            if (!searchUserId.trim()) return;
            try {
                setLoading(true);
                setActiveInspectorDoc(null);
                setUserDocs([]);
                setIsProfileReady(false);

                // 👑 ФІКС 404 для метаданих: якщо документів немає — ловимо 404 і залишаємо порожній масив
                let meta = [];
                try {
                    meta = await documentService.getMetadata(searchUserId.trim());
                } catch (err) {
                    if (err.response?.status === 404) {
                        meta = [];
                    } else {
                        throw err; // Якщо помилка інша (напр. 500 або 401) — передаємо її далі
                    }
                }

                // 👑 ФІКС 404 для статусу готовності профілю
                let status = false;
                try {
                    status = await documentService.getProfileStatus(searchUserId.trim());
                } catch (err) {
                    if (err.response?.status === 404) {
                        status = false;
                    } else {
                        throw err;
                    }
                }

                setUserDocs(meta || []);
                setIsProfileReady(status);

                // Якщо повернувся порожній масив або ми обробили 404 — показуємо інформаційне повідомлення
                if (!meta || meta.length === 0) {
                    toast.info("Цей користувач ще не підвантажував жодного документа. 📄");
                }
            } catch (err) {
                console.error("Критична помилка інспекції KYC:", err);
                toast.error("Не вдалося завантажити дані KYC для вказаного ID.");
                setUserDocs([]);
            } finally {
                setLoading(false);
            }
        };

    const handleVerifyDoc = async (docId) => {
        try {
            await documentService.verifyDocument(docId);
            toast.success("Документ успішно затверджено! ✅");

            // Миттєво оновлюємо локальний стейт
            setUserDocs(prev => prev.map(d => d.id === docId ? { ...d, isVerified: true } : d));

            // Оновлюємо загальну готовність профілю
            const status = await documentService.getProfileStatus(searchUserId.trim());
            setIsProfileReady(status);
        } catch (err) {
            toast.error("Не вдалося верифікувати документ.");
        }
    };

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>🛡️ Черга верифікації користувачів (KYC)</h1>

            {/* Панель пошуку за ID */}
            <form onSubmit={handleInspectUser} style={{ display: 'flex', gap: '10px', marginBottom: '25px', background: '#fff', padding: '15px', borderRadius: '8px', border: '1px solid #eee' }}>
                <input
                    type="number"
                    placeholder="Введіть ID користувача для перевірки (напр. 105)..."
                    value={searchUserId}
                    onChange={(e) => setSearchUserId(e.target.value)}
                    style={{ flex: 1, padding: '10px', borderRadius: '4px', border: '1px solid #ccc', fontSize: '14px' }}
                    required
                />
                <button type="submit" style={{ padding: '10px 20px', background: '#0056b3', color: '#fff', border: 'none', borderRadius: '4px', fontWeight: 'bold', cursor: 'pointer' }}>
                    Інспектувати профайл 🔍
                </button>
            </form>

            {loading ? (
                <div className={styles.loader} style={{ textAlign: 'center', padding: '30px' }}>Завантаження документів з сервера... ⏳</div>
            ) : userDocs.length > 0 ? (
                <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '30px' }}>

                    {/* Список завантажених документів */}
                    <div>
                        <div style={{ padding: '10px 15px', borderRadius: '6px', marginBottom: '15px', background: isProfileReady ? '#e6f7ff' : '#fff7e6', border: isProfileReady ? '1px solid #91d5ff' : '1px solid #ffd591', fontSize: '14px', fontWeight: 'bold' }}>
                            📊 Загальний статус профілю водія: {isProfileReady ? '🟢 ГОТОВИЙ ДО БРОНЮВАННЯ (Всі 3 документи затверджено)' : '🟡 БЛОКОВАНИЙ (Не всі файли апрувнуті адміном)'}
                        </div>

                        <table className={styles.table} style={{ width: '100%', background: '#fff', borderRadius: '8px', borderCollapse: 'collapse', border: '1px solid #eee' }}>
                            <thead>
                            <tr style={{ background: '#f8f9fa', borderBottom: '2px solid #eee' }}>
                                <th style={{ padding: '12px', textAlign: 'left' }}>Тип документа</th>
                                <th style={{ padding: '12px', textAlign: 'left' }}>Назва файлу</th>
                                <th style={{ padding: '12px', textAlign: 'center' }}>Статус</th>
                                <th style={{ padding: '12px', textAlign: 'center' }}>Дії</th>
                            </tr>
                            </thead>
                            <tbody>
                            {userDocs.map(doc => (
                                <tr key={doc.id} style={{ borderBottom: '1px solid #eee', cursor: 'pointer', background: activeInspectorDoc?.id === doc.id ? '#f4f7fe' : 'transparent' }} onClick={() => setActiveInspectorDoc(doc)}>
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

                    {/* Інспектор медіафайлу (Права панель) */}
                    <div style={{ background: '#fff', padding: '20px', borderRadius: '8px', border: '1px solid #eee', display: 'flex', flexDirection: 'column' }}>
                        <h3 style={{ margin: '0 0 15px 0', fontSize: '16px' }}>🖼️ Вікно перегляду документа</h3>
                        {activeInspectorDoc ? (
                            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '15px' }}>
                                <div style={{ fontSize: '13px', color: '#4a5568', background: '#f8f9fa', padding: '10px', borderRadius: '4px' }}>
                                    <strong>Тип:</strong> {activeInspectorDoc.documentType} <br/>
                                    <strong>Формат:</strong> {activeInspectorDoc.contentType}
                                </div>

                                {/* 👑 Використовуємо SecureImage для прямого стримінгу байтів документа за його токеном */}
                                <div style={{ flex: 1, border: '1px dashed #ccc', borderRadius: '6px', overflow: 'hidden', minHeight: '250px', background: '#fafafa', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                    {activeInspectorDoc.contentType.includes('image') ? (
                                        <SecureImage
                                            src={`/document/v1/${activeInspectorDoc.id}/download`}
                                            alt="KYC User document"
                                            style={{ maxWidth: '100%', maxHeight: '350px', objectFit: 'contain' }}
                                        />
                                    ) : (
                                        <div style={{ textAlign: 'center', padding: '20px' }}>
                                            <div style={{ fontSize: '40px' }}>📄</div>
                                            <div style={{ fontSize: '13px', marginTop: '5px' }}>Документ завантажено як <strong>PDF</strong>.</div>
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
                            <div style={{ textTransform: 'center', color: '#888', fontStyle: 'italic', paddingTop: '60px', textAlign: 'center' }}>
                                👈 Клікніть на будь-який рядок документа в таблиці для перегляду зображення
                            </div>
                        )}
                    </div>

                </div>
            ) : (
                <div style={{ background: '#fff', padding: '30px', borderRadius: '8px', border: '1px solid #eee', textAlign: 'center', color: '#666' }}>
                    🔍 Введіть ID водія у формі вище, щоб завантажити його документи для державної перевірки.
                </div>
            )}
        </div>
    );
};

export default KycManagement;
