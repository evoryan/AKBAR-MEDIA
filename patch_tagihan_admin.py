import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Add admin_name to tagihan_bulanan table creation
target_table = """        await req.pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            customer_id INT,
            bulan VARCHAR(50),
            tahun INT,
            amount DECIMAL(15, 2),
            status VARCHAR(50) DEFAULT 'BELUM BAYAR',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
        )`).catch(e=>{});"""

rep_table = """        await req.pool.query(`ALTER TABLE tagihan_bulanan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)`).catch(e=>{});
        await req.pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            customer_id INT,
            bulan VARCHAR(50),
            tahun INT,
            amount DECIMAL(15, 2),
            status VARCHAR(50) DEFAULT 'BELUM BAYAR',
            admin_name VARCHAR(100),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
        )`).catch(e=>{});"""

content = content.replace(target_table, rep_table)

# In `/api/billing/pay`, update admin_name in tagihan_bulanan
target_pay = """        if (tagihan.length > 0) {
            await req.pool.query('UPDATE tagihan_bulanan SET status = "LUNAS CASH" WHERE id = ?', [tagihan[0].id]);
            desc = `Pembayaran tagihan pelanggan ${customerName} (${tagihan[0].bulan} ${tagihan[0].tahun})`;
        }"""

rep_pay = """        if (tagihan.length > 0) {
            await req.pool.query('ALTER TABLE tagihan_bulanan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)').catch(e=>{});
            await req.pool.query('UPDATE tagihan_bulanan SET status = "LUNAS CASH", admin_name = ? WHERE id = ?', [adminName, tagihan[0].id]);
            desc = `Pembayaran tagihan pelanggan ${customerName} (${tagihan[0].bulan} ${tagihan[0].tahun})`;
        }"""

content = content.replace(target_pay, rep_pay)

# Add endpoint for `GET /api/pembayaran`
pembayaran_endpoint = """
app.get('/api/pembayaran', async (req, res) => {
    try {
        await req.pool.query('ALTER TABLE tagihan_bulanan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)').catch(e=>{});
        const [rows] = await req.pool.query(`
            SELECT 
                t.id, t.bulan, t.tahun, t.amount, t.admin_name, t.created_at,
                c.name as customer_name, c.phone, c.address as area
            FROM tagihan_bulanan t
            JOIN customers c ON t.customer_id = c.id
            WHERE t.status = 'LUNAS CASH'
            ORDER BY t.created_at DESC
        `);
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});
"""
if "app.get('/api/pembayaran'" not in content:
    content = content.replace("app.get('/api/pembukuan/all'", pembayaran_endpoint + "\napp.get('/api/pembukuan/all'")

with open("VPS/server.js", "w") as f:
    f.write(content)
