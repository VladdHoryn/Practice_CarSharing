import React, { useState } from 'react';
import styles from './RentalTermsPage.module.css';

const RentalTermsPage = () => {
    const [activeFaq, setActiveFaq] = useState(null);

    const faqs = [
        { q: 'Які документи потрібні?', a: 'Для громадян України: оригінал паспорта/ID-картки, ІПН та посвідчення водія категорії В. Для іноземців: міжнародний паспорт та водійські права міжнародного зразка.' },
        { q: 'Чи можна виїжджати за межі області?', a: 'Так, пересування на наших автомобілях дозволено по всій території України, окрім тимчасово окупованих територій та зон ведення бойових дій.' },
        { q: 'Що відбувається із заставою при ДТП?', a: 'Якщо ви не є винуватцем ДТП, застава повертається повністю. Якщо винуватець ви — збитки покриваються страховкою, але в межах суми вашої франшизи (застави).' }
    ];

    return (
        <div className={styles.container}>
            <h1 className={styles.mainTitle}>Умови оренди автомобілів</h1>

            {/* Кроки вимог */}
            <div className={styles.grid}>
                <div className={styles.card}>
                    <h3>🪪 Вимоги до водія</h3>
                    <ul className={styles.list}>
                        <li>Вік від <strong>23 років</strong></li>
                        <li>Стаж водіння не менше <strong>1 року</strong></li>
                        <li>Наявність дійсного водійського посвідчення категорії «B»</li>
                    </ul>
                </div>

                <div className={styles.card}>
                    <h3>🪙 Оплата та Застава</h3>
                    <ul className={styles.list}>
                        <li>Готівковий розрахунок або банківська картка</li>
                        <li>Застава вноситься під час оформлення договору (залежить від класу авто)</li>
                        <li>Повне повернення застави відразу після здачі чистий автомобіля</li>
                    </ul>
                </div>
            </div>

            {/* Ліміти та Страховка */}
            <div className={styles.highlightBanner}>
                <div className={styles.bannerItem}>
                    <h4>🛡️ Страхування</h4>
                    <p>Кожен автомобіль застрахований за пакетами ОСЦПВ та КАСКО із суворою франшизою.</p>
                </div>
                <div className={styles.bannerItem}>
                    <h4>🚀 Ліміт пробігу</h4>
                    <p>Добовий ліміт — <strong>300 км</strong>. Перевищення ліміту тарифікується додатково згідно з класом машини.</p>
                </div>
            </div>

            {/* FAQ замість відгуків */}
            <section className={styles.faqSection}>
                <h2 className={styles.subTitle}>Часті запитання (FAQ)</h2>
                <div className={styles.faqList}>
                    {faqs.map((faq, index) => (
                        <div key={index} className={`${styles.faqItem} ${activeFaq === index ? styles.open : ''}`} onClick={() => setActiveFaq(activeFaq === index ? null : index)}>
                            <div className={styles.faqQuestion}>
                                <span>{faq.q}</span>
                                <span className={styles.arrow}>▼</span>
                            </div>
                            <div className={styles.faqAnswer}>{faq.a}</div>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
};

export default RentalTermsPage;
