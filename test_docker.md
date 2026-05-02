# Перевірити users
docker exec -it user-db psql -U postgres -d user_service_db -c "SELECT id, email, role, is_active FROM users;"

# Перевірити cars
docker exec -it car-db psql -U postgres -d car_service_db -c "SELECT id, brand, model, car_class, price_per_day, user_id, status FROM cars;"

# Підрахувати кількість
docker exec -it car-db psql -U postgres -d car_service_db -c "SELECT COUNT(*) FROM cars;"


# Перевірити bookings таблицю
docker exec -it booking-db psql -U postgres -d booking_service_db -c "SELECT id, user_id, car_id, status, total_price FROM bookings;"

# Підрахувати кількість за статусами
docker exec -it booking-db psql -U postgres -d booking_service_db -c "SELECT status, COUNT(*) FROM bookings GROUP BY status;"

# Перевірити зв'язки (чи всі car_id існують в car-service)
docker exec -it booking-db psql -U postgres -d booking_service_db -c "SELECT DISTINCT car_id FROM bookings ORDER BY car_id;"
