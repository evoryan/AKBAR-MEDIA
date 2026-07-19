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

CREATE TABLE IF NOT EXISTS tenant_whatsapp_sessions (
    tenant_id INT PRIMARY KEY,
    status VARCHAR(50) NOT NULL DEFAULT 'DISCONNECTED',
    bot_number VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES users(id) ON DELETE CASCADE
);
