with open('VPS/server.js', 'r') as f:
    content = f.read()

old_insert = """        await pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
            ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`]);"""

new_insert = """        await pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
            ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`, 'Transaksi Cash']);"""

content = content.replace(old_insert, new_insert)
with open('VPS/server.js', 'w') as f:
    f.write(content)
