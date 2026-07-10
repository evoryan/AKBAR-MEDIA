CREATE DATABASE IF NOT EXISTS akbar_media_master;
USE akbar_media_master;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    db_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO users (name, username, password, role, db_name) 
VALUES ('Super Admin', 'akbar2026', '08Delapan', 'SUPER_ADMIN', 'app_db');
