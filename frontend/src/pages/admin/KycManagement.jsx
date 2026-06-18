import React, { useState, useEffect } from 'react';
import styles from './KycManagement.module.css';

const KycManagement = () => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        // ТЗ каже: "Верифікація буде готова пізніше", тому залишаємо тимчасовий мок
        setRequests([
            { id: 105, fullName: 'Максим Журик', email: 'maks.zh@gmail.com', documentType: 'Посвідчення водія (Категорія B)' }
        ]);
    }, []);

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>🛡️ Черга верифікації користувачів (KYC)</h1>
            <div className={styles.emptyState}>⚙️ Модуль інтеграції сервісу верифікації документів знаходиться в розробці бекенду.</div>
        </div>
    );
};

export default KycManagement;
