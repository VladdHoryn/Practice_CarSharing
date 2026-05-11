import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './LoginPage.module.css';
import { authService } from '../services/auth.service'; // Підключаємо сервіс

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

      const loginData = {
        email: credentials.email,
        password: credentials.password
      };

      console.log('Спроба входу з даними:', loginData);

      // Виклик API
      const response = await authService.login(loginData);

      // Якщо все ок, об'єкт юзера вже зберігся в localStorage (через auth.service)
      alert(response.message || "Вхід успішний!");

      // Перенаправляємо в каталог
      navigate('/catalog');

    } catch (err) {
      console.error('Помилка входу:', err);
      // Відображаємо помилку від бекенду ("Invalid email or password" або "Account is deactivated")
      setError(err.response?.data?.message || "Невірний email або пароль.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.authCard}>
        <h2 className={styles.title}>Вхід</h2>

        {/* Вивід помилки червоним кольором */}
        {error && <div style={{color: 'red', marginBottom: '15px', textAlign: 'center'}}>{error}</div>}

        <form onSubmit={handleSubmit}>
            <div className={styles.formGroup}>
                <label className={styles.label}>Email</label>
                <input
                    type="email"
                    name="email"
                    className={styles.input}
                    value={credentials.email}
                    onChange={handleChange}
                    required
                />
            </div>
          <div className={styles.formGroup}>
            <label className={styles.label}>Пароль</label>
            <div className={styles.inputWrapper}>
              <input
                type={showPassword ? "text" : "password"}
                name="password"
                className={styles.input}
                value={credentials.password}
                onChange={handleChange}
                required
              />
              <span
                className={styles.iconRight}
                onClick={() => setShowPassword(!showPassword)}
                style={{cursor: 'pointer'}}
              >
                👁️
              </span>
            </div>
          </div>

          <div className={styles.checkboxGroup}>
            <input
              type="checkbox"
              id="rememberMe"
              name="rememberMe"
              checked={credentials.rememberMe}
              onChange={handleChange}
            />
            <label htmlFor="rememberMe" className={styles.checkboxLabel}>Remember me</label>
          </div>

          <button type="submit" className={styles.submitBtn} disabled={isLoading}>
            {isLoading ? 'Завантаження...' : 'Підтвердити'}
          </button>
        </form>

        <div className={styles.footerLinks}>
          Don't have an account? <Link to="/register">Sign up</Link>
        </div>

        <div className={styles.socialLogin}>
          Or sign in with
          <div className={styles.socialIcons}>
            <div className={styles.socialIcon}>F</div>
            <div className={styles.socialIcon}>G</div>
            <div className={styles.socialIcon}>T</div>
            <div className={styles.socialIcon}>in</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
