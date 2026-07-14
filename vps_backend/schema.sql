CREATE TABLE IF NOT EXISTS customers (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255),
    phone VARCHAR(50),
    area VARCHAR(100),
    username VARCHAR(100),
    billingDate VARCHAR(10), -- e.g., '15' for 15th of every month
    status VARCHAR(50),
    price DECIMAL(10, 2)
);

CREATE TABLE IF NOT EXISTS tagihan_bulanan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(255),
    bulan VARCHAR(50),
    tahun INT,
    amount DECIMAL(10, 2),
    status VARCHAR(50) DEFAULT 'BELUM BAYAR', -- 'BELUM BAYAR', 'LUNAS CASH'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS pembukuan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50), -- 'Pemasukan', 'Pengeluaran'
    category VARCHAR(100),
    amount DECIMAL(10, 2),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
