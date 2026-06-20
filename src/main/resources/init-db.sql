-- 1. Tạo bảng users trước
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Tạo bảng health_metrics (Quan hệ 1:1 với users)
CREATE TABLE IF NOT EXISTS health_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    height DOUBLE NOT NULL,
    weight DOUBLE NOT NULL,
    bmi DOUBLE,
    bmr DOUBLE,
    tdee DOUBLE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Tạo bảng diet_plans (Quan hệ 1:n với users)
CREATE TABLE IF NOT EXISTS diet_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal VARCHAR(50) NOT NULL,
    menu_json JSON NOT NULL, -- MySQL hỗ trợ kiểu JSON rất mạnh mẽ
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Tạo bảng daily_logs (Quan hệ 1:n với users)
CREATE TABLE IF NOT EXISTS daily_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    calories_in INT DEFAULT 0,
    current_weight DOUBLE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_date (user_id, log_date) -- Mỗi ngày chỉ log 1 dòng duy nhất
);

-- 5. Chèn dữ liệu mẫu (Seed Data) để tiện test
INSERT INTO users (id, username, password, email) 
VALUES (1, 'intern_test', '$2a$10$Xptf.d.f4Yp1...', 'test@fitness.com')
ON DUPLICATE KEY UPDATE id=id;