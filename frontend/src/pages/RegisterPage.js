import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import styles from './RegisterPage.module.css';
import { authService } from '../services/auth.service';
import { toast } from 'react-toastify'; // Підключаємо красиві сповіщення

const RegisterPage = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    isOwner: false
    // Remember me видалено
  });

  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (formData.password !== formData.confirmPassword) {
      setError("Паролі не співпадають!");
      toast.warning("Паролі не співпадають!");
      return;
    }

    try {
      setIsLoading(true);

      const userData = {
        fullName: `${formData.firstName} ${formData.lastName}`.trim(),
        email: formData.email,
        password: formData.password,
        role: formData.isOwner ? 'OWNER' : 'RENTER'
      };

      await authService.register(userData);

      // Виводимо красиве сповіщення замість alert
      toast.success('Реєстрація успішна! 🎉 Тепер ви можете увійти.');
      navigate('/login');

    } catch (err) {
      console.error('Помилка реєстрації:', err);
      const errorMsg = err.response?.data?.message || "Помилка при реєстрації. Спробуйте ще раз.";
      setError(errorMsg);
      toast.error(errorMsg); // Виводимо помилку гарним вікном
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.authCard}>
        <h2 className={styles.title}>Реєстрація</h2>

        {error && <div style={{color: 'red', marginBottom: '15px', textAlign: 'center'}}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label className={styles.label}>Ім'я</label>
            <input type="text" name="firstName" className={styles.input} value={formData.firstName} onChange={handleChange} required />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Прізвище</label>
            <input type="text" name="lastName" className={styles.input} value={formData.lastName} onChange={handleChange} required />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Email</label>
            <input type="email" name="email" className={styles.input} value={formData.email} onChange={handleChange} required />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Phone</label>
            <input type="tel" name="phone" className={styles.input} value={formData.phone} onChange={handleChange} required />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Пароль</label>
            <div className={styles.inputWrapper}>
              <input type={showPassword ? "text" : "password"} name="password" className={styles.input} value={formData.password} onChange={handleChange} required />
              <span className={styles.iconRight} onClick={() => setShowPassword(!showPassword)} style={{cursor: 'pointer'}}>👁️</span>
            </div>
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Пароль (підтвердження)</label>
            <div className={styles.inputWrapper}>
              <input type={showPassword ? "text" : "password"} name="confirmPassword" className={styles.input} value={formData.confirmPassword} onChange={handleChange} required />
              <span className={styles.iconRight} onClick={() => setShowPassword(!showPassword)} style={{cursor: 'pointer'}}>👁️</span>
            </div>
          </div>

          {/* Галочка Remember me ПРИБРАНА */}

          <div className={styles.roleToggle}>
            <span style={{ color: !formData.isOwner ? '#333' : '#999' }}>Я хочу орендувати</span>
            <label className={styles.switch}>
              <input type="checkbox" name="isOwner" checked={formData.isOwner} onChange={handleChange} />
              <span className={styles.slider}></span>
            </label>
            <span style={{ color: formData.isOwner ? '#333' : '#999' }}>Я хочу здати автомобіль</span>
          </div>

          <button type="submit" className={styles.submitBtn} disabled={isLoading}>
            {isLoading ? 'Завантаження...' : 'Підтвердити'}
          </button>
        </form>

        <div className={styles.footerLinks}>
          Already have an account? <Link to="/login">Sign in</Link>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
