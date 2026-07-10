import re
with open('VPS/init.sql', 'r') as f:
    content = f.read()

# Make sure we add category field
old_pembukuan = """CREATE TABLE IF NOT EXISTS pembukuan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('pemasukan', 'pengeluaran'),
    amount DECIMAL(15,2),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO pembukuan (type, amount, description) VALUES('pemasukan', 2575000, 'Saldo Awal');"""

new_pembukuan = """CREATE TABLE IF NOT EXISTS pembukuan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    type ENUM('pemasukan', 'pengeluaran'),
    category VARCHAR(50) DEFAULT 'Lain-lain',
    amount DECIMAL(15,2),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
"""

if "category VARCHAR(50)" not in content:
    content = content.replace(old_pembukuan, new_pembukuan)
    with open('VPS/init.sql', 'w') as f:
        f.write(content)
