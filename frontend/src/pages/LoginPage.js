import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './LoginPage.module.css';
import { authService } from '../services/auth.service';
import { toast } from 'react-toastify'; // Підключаємо сповіщення

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

        const response = await authService.login(loginData);
        if (response && (response.status === 401 || response.error)) {
            throw new Error(response.message || "Невірний email або пароль.");
        }


        if (!localStorage.getItem('user')) {
            throw new Error("Невірний email або пароль.");
        }

        toast.success('Вхід успішний! 👋');
        navigate('/catalog');

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

        {error && <div style={{color: 'red', marginBottom: '15px', textAlign: 'center'}}>{error}</div>}

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
