import React, { useState, useEffect } from 'react';
import { userService } from '../../services/user.service';
import { toast } from 'react-toastify';
import styles from './UserManagement.module.css';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    // 👑 ДОДАНО: Локальне сортування та фільтрація
    const [filterStatus, setFilterStatus] = useState('ALL');
    const [sortKey, setSortByKey] = useState('fullName');

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const data = await userService.getAllUsers();
            setUsers(data || []);
        } catch (err) {
            console.error('Помилка завантаження користувачів:', err);
            toast.error('Не вдалося завантажити список користувачів.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleToggleBlock = async (user) => {
        const actionText = user.isActive ? 'деактивувати' : 'активувати';
        if (!window.confirm(`Ви впевнені, що хочете ${actionText} користувача ${user.fullName}?`)) return;

        try {
            if (user.isActive) {
                await userService.deactivateUserByKeycloak(user.keycloakId);
                toast.warning(`Користувача ${user.fullName} заблоковано.`);
            } else {
                await userService.activateUserByKeycloak(user.keycloakId);
                toast.success(`Користувача ${user.fullName} активовано!`);
            }
            fetchUsers();
        } catch (err) {
            toast.error('Помилка при зміні статусу користувача. Перевірте CORS PATCH на шлюзі.');
        }
    };

    // 👑 ДОДАНО: Обробка пошуку, фільтрації за статусом та сортування на фронті
    const processedUsers = users
        .filter(user => {
            const matchesSearch = user.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                user.email?.toLowerCase().includes(searchTerm.toLowerCase());
            const matchesStatus = filterStatus === 'ALL' ||
                (filterStatus === 'ACTIVE' && user.isActive) ||
                (filterStatus === 'BLOCKED' && !user.isActive);
            return matchesSearch && matchesStatus;
        })
        .sort((a, b) => {
            if (!a[sortKey] || !b[sortKey]) return 0;
            return a[sortKey].toString().localeCompare(b[sortKey].toString());
        });

    return (
        <div className={styles.container}>
            <div className={styles.tableHeader}>
                <h1 className={styles.title}>👥 Керування користувачами</h1>
                <div style={{ display: 'flex', gap: '10px' }}>
                    <input
                        type="text"
                        placeholder="Пошук за Email або ПІБ..."
                        className={styles.searchInput}
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)} className={styles.searchInput} style={{ width: '160px' }}>
                        <option value="ALL">Всі статуси</option>
                        <option value="ACTIVE">⚡ Active</option>
                        <option value="BLOCKED">🔒 Blocked</option>
                    </select>
                    <select value={sortKey} onChange={(e) => setSortByKey(e.target.value)} className={styles.searchInput} style={{ width: '160px' }}>
                        <option value="fullName">Сортувати: ПІБ</option>
                        <option value="email">Сортувати: Email</option>
                    </select>
                </div>
            </div>

            {loading ? (
                <div className={styles.loader}>Завантаження списку користувачів... ⏳</div>
            ) : (
                <div className={styles.tableWrapper}>
                    <table className={styles.table}>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>ПІБ</th>
                            <th>Email</th>
                            <th>Роль</th>
                            <th>Статус</th>
                            <th>Дії</th>
                        </tr>
                        </thead>
                        <tbody>
                        {processedUsers.map((user) => (
                            <tr key={user.id}>
                                <td>#{user.id}</td>
                                <td><strong>{user.fullName}</strong></td>
                                <td>{user.email}</td>
                                <td><span className={`${styles.roleBadge} ${styles[user.role?.toLowerCase() || 'renter']}`}>{user.role}</span></td>
                                <td>
                                    <span className={user.isActive ? styles.statusActive : styles.statusBlocked}>
                                        {user.isActive ? '● Active' : '● Blocked'}
                                    </span>
                                </td>
                                <td>
                                    <button onClick={() => handleToggleBlock(user)} className={user.isActive ? styles.deleteBtn : styles.editBtn} style={{ padding: '5px 10px', borderRadius: '4px', cursor: 'pointer' }}>
                                        {user.isActive ? '🔒 Блок' : '🔓 Розблок'}
                                    </button>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default UserManagement;
