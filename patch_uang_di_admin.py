import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Add admin_name column in initAllDatabases
content = content.replace('await masterPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT \\\'Lain-lain\\\'`).catch(e=>{});', 
'''await masterPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`).catch(e=>{});
        await masterPool.query(`ALTER TABLE pembukuan ADD COLUMN admin_name VARCHAR(100) DEFAULT ''`).catch(e=>{});''')

content = content.replace('await tPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT \\\'Lain-lain\\\'`).catch(e=>{});', 
'''await tPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`).catch(e=>{});
            await tPool.query(`ALTER TABLE pembukuan ADD COLUMN admin_name VARCHAR(100) DEFAULT ''`).catch(e=>{});''')

# Update /api/billing/pay to insert admin_name
old_pay = """        // Add to pembukuan
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                 ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`, 'Transaksi Cash']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                 ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`]);
        }"""

new_pay = """        // Add to pembukuan
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category, admin_name) VALUES (?, ?, ?, ?, ?)', 
                 ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`, 'Transaksi Cash', adminName || '']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            try {
                await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                     ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`, 'Transaksi Cash']);
            } catch (e2) {
                await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                     ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`]);
            }
        }"""

content = content.replace(old_pay, new_pay)

# Add /api/uang-di-admin endpoint
new_endpoint = """
app.get('/api/uang-di-admin', async (req, res) => {
    try {
        const [rows] = await req.pool.query(`
            SELECT admin_name as adminName, SUM(amount) as totalAmount 
            FROM pembukuan 
            WHERE category = 'Transaksi Cash' AND type = 'pemasukan' 
            GROUP BY admin_name
        `);
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});
"""

if "app.get('/api/uang-di-admin'" not in content:
    content = content.replace("app.get('/api/pembukuan'", new_endpoint + "app.get('/api/pembukuan'")

with open('VPS/server.js', 'w') as f:
    f.write(content)
