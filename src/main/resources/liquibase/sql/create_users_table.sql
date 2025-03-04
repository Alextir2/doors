CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       phone_number VARCHAR(15) UNIQUE NOT NULL,
                       name VARCHAR(255),
                       role VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Column comments

COMMENT ON TABLE users IS 'Таблица пользователей';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя';
COMMENT ON COLUMN users.phone_number IS 'Номер телефона пользователя';
COMMENT ON COLUMN users.name IS 'Имя пользователя';
COMMENT ON COLUMN users.role IS 'Роль пользователя в системе';
COMMENT ON COLUMN users.created_at IS 'Дата и время создания пользователя';