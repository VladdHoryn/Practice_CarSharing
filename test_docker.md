# Перевірити users
docker exec -it user-db psql -U postgres -d user_service_db -c "SELECT id, email, role, is_active FROM users;"

# Перевірити cars
docker exec -it car-db psql -U postgres -d car_service_db -c "SELECT id, brand, model, car_class, price_per_day, user_id, status FROM cars;"

# Підрахувати кількість
docker exec -it car-db psql -U postgres -d car_service_db -c "SELECT COUNT(*) FROM cars;"
