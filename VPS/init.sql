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

CREATE TABLE IF NOT EXISTS odc_list (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    location VARCHAR(255),
    portCount INT DEFAULT 0,
    portInput VARCHAR(100) DEFAULT ''
);CREATE TABLE IF NOT EXISTS odp_list (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    odcId INT,
    portCount INT DEFAULT 0,
    portInput VARCHAR(100) DEFAULT '',
    FOREIGN KEY (odcId) REFERENCES odc_list(id) ON DELETE CASCADE
);CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    phone VARCHAR(20),
    area VARCHAR(50),
    address TEXT,
    username VARCHAR(50),
    billingDate VARCHAR(10),
    status VARCHAR(20),
    price VARCHAR(50),
    discount VARCHAR(50),
    register_date VARCHAR(50) DEFAULT "",
    isolate_date VARCHAR(50) DEFAULT "",
    package_name VARCHAR(100) DEFAULT "",
    pppoe_secret VARCHAR(100) DEFAULT "",
    odp_id INT DEFAULT NULL,
    odp_port VARCHAR(10) DEFAULT "",
    FOREIGN KEY (odp_id) REFERENCES odp_list(id) ON DELETE SET NULL
);


CREATE TABLE IF NOT EXISTS tagihan_bulanan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    bulan VARCHAR(50),
    tahun INT,
    amount DECIMAL(10, 2),
    status VARCHAR(50) DEFAULT 'BELUM BAYAR', -- 'BELUM BAYAR', 'LUNAS CASH'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pembukuan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('pemasukan', 'pengeluaran'),
    amount DECIMAL(15,2),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


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








CREATE TABLE IF NOT EXISTS categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100)
);


CREATE TABLE IF NOT EXISTS inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    categoryId INT,
    stock INT,
    FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS stock_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50),
    itemName VARCHAR(100),
    quantity INT,
    adminName VARCHAR(100),
    timestamp BIGINT
);


CREATE TABLE IF NOT EXISTS packages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    speed VARCHAR(50),
    price DECIMAL(15,2),
    taxRate DECIMAL(5,2),
    pppoeProfile VARCHAR(100),
    description TEXT
);

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
