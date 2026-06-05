import React, { useState, useEffect } from 'react';
import { userService } from '../../services/user.service';
import { toast } from 'react-toastify';
import styles from './UserManagement.module.css';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    const [currentPage, setCurrentPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);
    const pageSize = 10;

    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [newRole, setNewRole] = useState('RENTER');

    const fetchUsers = async () => {
        try {
            setLoading(true);
            const params = {
                page: currentPage,
                size: pageSize,
                search: searchTerm
            };
            const data = await userService.getAllUsers(params);


            setUsers(data.content || data);
            setTotalPages(data.totalPages || 1);
        } catch (err) {
            console.error('Помилка завантаження користувачів:', err);

            setUsers([
                { id: 12, fullName: 'Zhuryk Maks', email: 'zhuryk@carsharing.com', role: 'ADMINISTRATOR', isActive: true },
                { id: 101, fullName: 'Олексій Коваленко', email: 'olex@gmail.com', role: 'RENTER', isActive: true },
                { id: 102, fullName: 'Марія Петренко', email: 'maria.p@gmail.com', role: 'OWNER', isActive: false },
            ]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, [currentPage, searchTerm]);

    const handleSearchChange = (e) => {
        setSearchTerm(e.target.value);
        setCurrentPage(0);
    };

    const openEditModal = (user) => {
        setSelectedUser(user);
        setNewRole(user.role);
        setIsEditModalOpen(true);
    };

    const handleUpdateRole = async () => {
        try {
            await userService.updateUserRole(selectedUser.id, newRole);
            toast.success(`Роль користувача ${selectedUser.fullName} успішно змінено!`);
            setIsEditModalOpen(false);
            fetchUsers();
        } catch (err) {
            toast.error('Не вдалося оновити роль.');
        }
    };

    const openDeleteModal = (user) => {
        setSelectedUser(user);
        setIsDeleteModalOpen(true);
    };

    const handleDeleteUser = async () => {
        try {
            await userService.deleteUser(selectedUser.id);
            toast.success(`Користувача заблоковано/видалено.`);
            setIsDeleteModalOpen(false);
            fetchUsers();
        } catch (err) {
            toast.error('Помилка при видаленні користувача.');
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.tableHeader}>
                <h1 className={styles.title}>👥 Керування користувачами</h1>
                <input
                    type="text"
                    placeholder="Пошук за Email або ПІБ..."
                    className={styles.searchInput}
                    value={searchTerm}
                    onChange={handleSearchChange}
                />
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
                        {users.map((user) => (
                            <tr key={user.id}>
                                <td>#{user.id}</td>
                                <td><strong>{user.fullName}</strong></td>
                                <td>{user.email}</td>
                                <td>
                                        <span className={`${styles.roleBadge} ${styles[user.role.toLowerCase()]}`}>
                                            {user.role}
                                        </span>
                                </td>
                                <td>
                                        <span className={user.isActive ? styles.statusActive : styles.statusBlocked}>
                                            {user.isActive ? '● Active' : '● Blocked'}
                                        </span>
                                </td>
                                <td>
                                    <div className={styles.actions}>
                                        <button onClick={() => openEditModal(user)} className={styles.editBtn}>✏️ Роль</button>
                                        <button onClick={() => openDeleteModal(user)} className={styles.deleteBtn}>🔒 Блок</button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>

                    {totalPages > 1 && (
                        <div className={styles.pagination}>
                            <button disabled={currentPage === 0} onClick={() => setCurrentPage(prev => prev - 1)}>« Назад</button>
                            <span>Сторінка {currentPage + 1} з {totalPages}</span>
                            <button disabled={currentPage >= totalPages - 1} onClick={() => setCurrentPage(prev => prev + 1)}>Вперед »</button>
                        </div>
                    )}
                </div>
            )}

            {isEditModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <h3>Зміна доступу для {selectedUser?.fullName}</h3>
                        <p>Оберіть нову системну роль користувача:</p>
                        <select value={newRole} onChange={(e) => setNewRole(e.target.value)} className={styles.select}>
                            <option value="RENTER">RENTER (Орендар)</option>
                            <option value="OWNER">OWNER (Власник авто)</option>
                            <option value="ADMINISTRATOR">ADMINISTRATOR (Адміністратор)</option>
                        </select>
                        <div className={styles.modalActions}>
                            <button onClick={() => setIsEditModalOpen(false)} className={styles.cancelBtn}>Скасувати</button>
                            <button onClick={handleUpdateRole} className={styles.saveBtn}>Зберегти зміни</button>
                        </div>
                    </div>
                </div>
            )}

            {isDeleteModalOpen && (
                <div className={styles.modalOverlay}>
                    <div className={styles.modal}>
                        <h3 style={{ color: '#ef4444' }}>⚠️ Підтвердження дії</h3>
                        <p>Ви впевнені, що хочете заблокувать доступ для <strong>{selectedUser?.fullName}</strong>?</p>
                        <p style={{ fontSize: '13px', color: '#64748b' }}>Користувач втратить можливість авторизуватися на платформі.</p>
                        <div className={styles.modalActions}>
                            <button onClick={() => setIsDeleteModalOpen(false)} className={styles.cancelBtn}>Ні, назад</button>
                            <button onClick={handleDeleteUser} className={styles.confirmDangerBtn}>Так, заблокувати</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserManagement;
