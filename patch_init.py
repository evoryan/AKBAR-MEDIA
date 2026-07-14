import re

with open("VPS/init.sql", "r") as f:
    content = f.read()

table_sql = """CREATE TABLE IF NOT EXISTS tagihan_bulanan (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    bulan VARCHAR(50),
    tahun INT,
    amount DECIMAL(10, 2),
    status VARCHAR(50) DEFAULT 'BELUM BAYAR', -- 'BELUM BAYAR', 'LUNAS CASH'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

"""

if "tagihan_bulanan" not in content:
    content = content.replace("CREATE TABLE IF NOT EXISTS pembukuan", table_sql + "CREATE TABLE IF NOT EXISTS pembukuan")
    with open("VPS/init.sql", "w") as f:
        f.write(content)
