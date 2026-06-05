import React from 'react';
import { toast } from 'react-toastify';
import styles from './ContactsPage.module.css';

const ContactsPage = () => {
    const handleSendMessage = (e) => {
        e.preventDefault();
        toast.success('Ваше повідомлення успішно надіслано! Менеджер звʼяжеться з вами. 📞');
        e.target.reset();
    };

    return (
        <div className={styles.container}>
            <h1 className={styles.title}>Контакти та зворотній зв'язок</h1>
            <div className={styles.layout}>

                <div className={styles.infoBlock}>
                    <h2>Наш центральний офіс</h2>
                    <p className={styles.desc}>Чекаємо на вас для оформлення договору та передачі ключів.</p>

                    <div className={styles.contactItem}>
                        <span className={styles.icon}>📍</span>
                        <div><strong>Адреса:</strong><p>вулиця Університетська, 28, Чернівці, Україна</p></div>
                    </div>
                    <div className={styles.contactItem}>
                        <span className={styles.icon}>📞</span>
                        <div><strong>Телефон:</strong><p>+38 (050) 123-45-67</p></div>
                    </div>
                    <div className={styles.contactItem}>
                        <span className={styles.icon}>✉️</span>
                        <div><strong>Email:</strong><p>support@carlink.com</p></div>
                    </div>
                </div>

                <div className={styles.formBlock}>
                    <h3>Залишилися запитання?</h3>
                    <form onSubmit={handleSendMessage} className={styles.form}>
                        <input type="text" required placeholder="Ваше ім'я" className={styles.input} />
                        <input type="email" required placeholder="Електронна пошта" className={styles.input} />
                        <textarea required rows="4" placeholder="Текст вашого запитання або пропозиції..." className={styles.textarea} />
                        <button type="submit" className={styles.submitBtn}>Надіслати повідомлення</button>
                    </form>
                </div>

            </div>
        </div>
    );
};

export default ContactsPage;
