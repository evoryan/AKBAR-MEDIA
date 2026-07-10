CREATE DATABASE IF NOT EXISTS app_db;
USE app_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    username VARCHAR(50) UNIQUE,
    role VARCHAR(50),
    password VARCHAR(255)
);

INSERT INTO users (name, username, role, password) VALUES
('Super Admin', 'superadmin', 'SUPER_ADMIN', 'password'),
('Admin Biasa', 'admin', 'ADMIN', 'password'),
('Teknisi 1', 'teknisi1', 'TEKNISI', 'password'),
('Collector 1', 'collector1', 'COLLECTOR', 'password');

CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    phone VARCHAR(20),
    area VARCHAR(50),
    username VARCHAR(50),
    billingDate VARCHAR(10),
    status VARCHAR(20),
    price VARCHAR(50),
    discount VARCHAR(50)
);

INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount) VALUES
('Budi Santoso', '081234567890', 'Talun', 'budi.talun', '15', 'Aktif', 'Rp. 150.000', '0'),
('Siti Aminah', '081987654321', 'Kedung', 'siti.kedung', '01', 'Nonaktif', 'Rp. 150.000', '0');

CREATE TABLE IF NOT EXISTS pembukuan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('pemasukan', 'pengeluaran'),
    amount DECIMAL(15,2),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO pembukuan (type, amount, description) VALUES
('pemasukan', 2575000, 'Saldo Awal');

CREATE TABLE IF NOT EXISTS areas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(255),
    customerCount INT,
    routerIp VARCHAR(50),
    apiDomain VARCHAR(100),
    mikrotikUser VARCHAR(255),
    mikrotikPassword VARCHAR(255),
    acsUser VARCHAR(255),
    acsPassword VARCHAR(255)
);

INSERT INTO areas (name, description, customerCount, routerIp, apiDomain) VALUES
('Talun', 'Area cakupan desa Talun dan sekitarnya', 15, '192.168.1.1:8728', 'http://192.168.1.1:7557'),
('Kedung', 'Area cakupan desa Kedung', 8, '192.168.2.1:8728', 'http://192.168.2.1:7557'),
('Bate', 'Area cakupan desa Bate, wilayah timur', 24, '192.168.3.1:8728', 'http://192.168.3.1:7557');

CREATE TABLE IF NOT EXISTS odc_list (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    location VARCHAR(255)
);

INSERT INTO odc_list (name, location) VALUES
('ODC-01', 'Pusat'),
('ODC-02', 'Cabang Utara');

CREATE TABLE IF NOT EXISTS odp_list (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    odcId INT,
    portCount INT,
    FOREIGN KEY (odcId) REFERENCES odc_list(id) ON DELETE CASCADE
);

INSERT INTO odp_list (name, odcId, portCount) VALUES
('ODP-01', 1, 8),
('ODP-02', 1, 16),
('ODP-03', 2, 8);

CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100)
);

INSERT INTO categories (name) VALUES ('Router'), ('Kabel'), ('Antena');

CREATE TABLE IF NOT EXISTS inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    categoryId INT,
    stock INT,
    FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE CASCADE
);

INSERT INTO inventory (name, categoryId, stock) VALUES
('Mikrotik RB750', 1, 10),
('Kabel UTP 100m', 2, 5),
('LiteBeam M5', 3, 8);

CREATE TABLE IF NOT EXISTS stock_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50),
    itemName VARCHAR(100),
    quantity INT,
    adminName VARCHAR(100),
    timestamp BIGINT
);

INSERT INTO stock_history (type, itemName, quantity, adminName, timestamp) VALUES
('IN', 'Mikrotik RB750', 10, 'Admin 1', 1672531200000),
('OUT', 'Kabel UTP 100m', 2, 'Admin 2', 1672534800000);

CREATE TABLE IF NOT EXISTS packages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    speed VARCHAR(50),
    price DECIMAL(15,2),
    taxRate DECIMAL(5,2),
    pppoeProfile VARCHAR(100),
    description TEXT
);
INSERT INTO packages (name, speed, price, taxRate, pppoeProfile, description) VALUES
('Paket Basic', '10 Mbps', 150000, 11.0, 'Profile-10M', 'Cocok untuk browsing'),
('Paket Pro', '20 Mbps', 250000, 11.0, 'Profile-20M', 'Cocok untuk streaming');

CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    message VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pengeluaran (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(255) UNIQUE,
    amount DECIMAL(15,2) DEFAULT 0,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
