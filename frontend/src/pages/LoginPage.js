import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './LoginPage.module.css';

const LoginPage = () => {
  const [credentials, setCredentials] = useState({
    email: '',
    password: '',
    rememberMe: false
  });

  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setCredentials(prevState => ({
      ...prevState,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Спроба входу з даними:', credentials);

    // TODO: Тут буде виклик API axios.post('/api/auth/login', credentials)
    // Якщо логін успішний:
    // 1. Зберігаємо токен (напр., localStorage.setItem('token', response.data.token))
    // 2. Перенаправляємо в каталог:
    navigate('/catalog');
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.authCard}>
        <h2 className={styles.title}>Вхід</h2>

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

          <button type="submit" className={styles.submitBtn}>
            Підтвердити
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
