import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './LoginPage.module.css';
import { authService } from '../services/auth.service';
import { toast } from 'react-toastify';
import { userService } from '../services/user.service';

const LoginPage = () => {
    const navigate = useNavigate();
    const [credentials, setCredentials] = useState({
        email: '',
        password: '',
        rememberMe: false
    });

    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setCredentials(prevState => ({
            ...prevState,
            [name]: type === 'checkbox' ? checked : value
        }));
    };

    const handleSubmit = async (e) => {
            e.preventDefault();
            setError(null);

            try {
                setIsLoading(true);
                const loginData = { email: credentials.email, password: credentials.password };

                // 1. Авторизуємося в Keycloak (отримуємо токени)
                const response = await authService.login(loginData);
                if (response && (response.status === 401 || response.error)) {
                    throw new Error(response.message || "Невірний email або пароль.");
                }

                const storedUser = localStorage.getItem('user');
                if (!storedUser) {
                    throw new Error("Невірний email або пароль.");
                }
                const keycloakUser = JSON.parse(storedUser);

                // 👑 КРИТИЧНИЙ ФІКС БЕЗПЕКИ: Миттєво йдемо в наш user-service за статусом із бази даних!
                try {
                    const realDbUser = await userService.getUserByKeycloakId(keycloakUser.id);

                    if (realDbUser && realDbUser.isActive === false) {
                        authService.logout(); // Очищаємо localStorage, закриваємо сесію
                        const blockedMsg = "🛑 Ваш обліковий запис заблоковано адміністратором. Доступ закрити.";
                        setError(blockedMsg);
                        toast.error(blockedMsg);
                        return; // Зупиняємо логін, в каталог не пускаємо!
                    }
                } catch (dbErr) {
                    console.error("Помилка верифікації статусу через DB:", dbErr);
                    // Якщо бази немає — страхуємося, але пускаємо, або блокуємо за твоїм вибором
                }

                toast.success('Вхід успішний! 👋');

                // Чистий редирект
                if (keycloakUser.role === 'ADMINISTRATOR') {
                    navigate('/admin/dashboard');
                } else {
                    navigate('/catalog');
                }

            } catch (err) {
                console.error('Помилка входу:', err);
                const errorMsg = err.response?.data?.message || err.message || "Невірний email або пароль.";
                setError(errorMsg);
                toast.error(errorMsg);
            } finally {
                setIsLoading(false);
            }
        };

    return (
        <div className={styles.pageContainer}>
            <div className={styles.authCard}>
                <h2 className={styles.title}>Вхід</h2>

                {error && <div style={{color: 'red', marginBottom: '15px', textAlign: 'center', fontSize: '13px', fontWeight: 'bold'}}>{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Email</label>
                        <input type="email" name="email" className={styles.input} value={credentials.email} onChange={handleChange} required />
                    </div>
                    <div className={styles.formGroup}>
                        <label className={styles.label}>Пароль</label>
                        <div className={styles.inputWrapper}>
                            <input type={showPassword ? "text" : "password"} name="password" className={styles.input} value={credentials.password} onChange={handleChange} required />
                            <span className={styles.iconRight} onClick={() => setShowPassword(!showPassword)} style={{cursor: 'pointer'}}>👁️</span>
                        </div>
                    </div>

                    <div className={styles.checkboxGroup}>
                        <input type="checkbox" id="rememberMe" name="rememberMe" checked={credentials.rememberMe} onChange={handleChange} />
                        <label htmlFor="rememberMe" className={styles.checkboxLabel}>Remember me</label>
                    </div>

                    <button type="submit" className={styles.submitBtn} disabled={isLoading}>
                        {isLoading ? 'Завантаження...' : 'Підтвердити'}
                    </button>
                </form>

                <div className={styles.footerLinks}>
                    Don't have an account? <Link to="/register">Sign up</Link>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
