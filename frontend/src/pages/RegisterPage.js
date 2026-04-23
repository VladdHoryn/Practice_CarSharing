import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import styles from './RegisterPage.module.css';

const RegisterPage = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    password: '',
    confirmPassword: '',
    isOwner: false, // false = орендар (RENTER), true = орендодавець (OWNER)
    rememberMe: false
  });

  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prevState => ({
      ...prevState,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Дані для відправки на бекенд:', formData);
    // Тут буде виклик axios.post('/api/users/register', formData)
  };

  return (
    <div className={styles.pageContainer}>
      <div className={styles.authCard}>
        <h2 className={styles.title}>Реєстрація</h2>

        <form onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label className={styles.label}>Ім'я</label>
            <input
              type="text"
              name="firstName"
              className={styles.input}
              value={formData.firstName}
              onChange={handleChange}
              required
            />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Прізвище</label>
            <input
              type="text"
              name="lastName"
              className={styles.input}
              value={formData.lastName}
              onChange={handleChange}
              required
            />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Email</label>
            <input
              type="email"
              name="email"
              className={styles.input}
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Phone</label>
            <input
              type="tel"
              name="phone"
              className={styles.input}
              value={formData.phone}
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
                value={formData.password}
                onChange={handleChange}
                required
              />
              {/* Тут можна вставити SVG іконку ока, для прикладу використано емодзі/текст */}
              <span
                className={styles.iconRight}
                onClick={() => setShowPassword(!showPassword)}
              >
                👁️
              </span>
            </div>
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label}>Пароль (підтвердження)</label>
            <div className={styles.inputWrapper}>
              <input
                type={showPassword ? "text" : "password"}
                name="confirmPassword"
                className={styles.input}
                value={formData.confirmPassword}
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
              id="remember"
              name="rememberMe"
              checked={formData.rememberMe}
              onChange={handleChange}
            />
            <label htmlFor="remember" className={styles.checkboxLabel}>Remember me</label>
          </div>

          {/* Світч ролей згідно з макетом */}
          <div className={styles.roleToggle}>
            <span style={{ color: !formData.isOwner ? '#333' : '#999' }}>Я хочу орендувати</span>
            <label className={styles.switch}>
              <input
                type="checkbox"
                name="isOwner"
                checked={formData.isOwner}
                onChange={handleChange}
              />
              <span className={styles.slider}></span>
            </label>
            <span style={{ color: formData.isOwner ? '#333' : '#999' }}>Я хочу здати автомобіль</span>
          </div>

          <button type="submit" className={styles.submitBtn}>
            Підтвердити
          </button>
        </form>

        <div className={styles.footerLinks}>
          Already have an account? <Link to="/login">Sign in</Link>
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

export default RegisterPage;
