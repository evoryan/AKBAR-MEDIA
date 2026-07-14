import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# 1. Add table creation
target_create = """        await masterPool.query(`CREATE TABLE IF NOT EXISTS pengeluaran ("""
rep_create = """        await masterPool.query(`CREATE TABLE IF NOT EXISTS pemasukan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            category VARCHAR(100) UNIQUE,
            amount DOUBLE,
            description TEXT,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )`).catch(e=>{});
        await masterPool.query(`CREATE TABLE IF NOT EXISTS pengeluaran ("""
content = content.replace(target_create, rep_create)

# 2. Add to pemasukan on billing/pay
target_pay = """        // Add to pembukuan
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc, 'Transaksi Cash']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc]);
        }"""
rep_pay = """        // Add to pembukuan
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc, 'Transaksi Cash']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc]);
        }
        
        // Add to pemasukan summary table
        try {
            await req.pool.query(
                'INSERT INTO pemasukan (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)',
                ['Transaksi Cash', totalAmount || 0, desc]
            );
        } catch (e) {
            console.error("Warning: failed to insert to pemasukan table", e.message);
        }"""
content = content.replace(target_pay, rep_pay)

# 3. Handle total pendapatan in dashboard/summary
target_summary = """        const [unpaidCustomers] = await req.pool.query('SELECT COUNT(*) as unpaid FROM customers WHERE status != "LUNAS CASH"');
        const [pembukuan] = await req.pool.query('SELECT type, SUM(amount) as total FROM pembukuan GROUP BY type');
        
        let monthlyRevenue = 0;
        pembukuan.forEach(row => {
            if (row.type === 'pemasukan') monthlyRevenue += Number(row.total);
        });"""
rep_summary = """        const [unpaidCustomers] = await req.pool.query('SELECT COUNT(*) as unpaid FROM customers WHERE status != "LUNAS CASH"');
        
        let totalPemasukan = 0;
        let totalPengeluaran = 0;
        try {
            const [pemasukanRows] = await req.pool.query('SELECT SUM(amount) as total FROM pemasukan');
            totalPemasukan = pemasukanRows[0]?.total || 0;
        } catch (e) {
            // fallback if table doesn't exist yet
            const [pembukuan] = await req.pool.query('SELECT type, SUM(amount) as total FROM pembukuan GROUP BY type');
            pembukuan.forEach(row => {
                if (row.type === 'pemasukan') totalPemasukan += Number(row.total);
            });
        }
        
        try {
            const [pengeluaranRows] = await req.pool.query('SELECT SUM(amount) as total FROM pengeluaran');
            totalPengeluaran = pengeluaranRows[0]?.total || 0;
        } catch (e) {
            const [pembukuan] = await req.pool.query('SELECT type, SUM(amount) as total FROM pembukuan GROUP BY type');
            pembukuan.forEach(row => {
                if (row.type === 'pengeluaran') totalPengeluaran += Number(row.total);
            });
        }
        
        const monthlyRevenue = totalPemasukan - totalPengeluaran;"""
content = content.replace(target_summary, rep_summary)

# 4. Expose GET /api/pemasukan and POST /api/pemasukan
target_pengeluaran_api = """app.get('/api/pengeluaran', async (req, res) => {"""
rep_pemasukan_api = """app.get('/api/pemasukan', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT category, amount, description, updated_at FROM pemasukan');
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.post('/api/pemasukan', async (req, res) => {
    try {
        const { category, amount, description } = req.body;
        await req.pool.query(
            'INSERT INTO pemasukan (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = VALUES(amount), description = VALUES(description)',
            [category, amount || 0, description || '']
        );
        res.json({ message: "Pemasukan diupdate" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/pengeluaran', async (req, res) => {"""
content = content.replace(target_pengeluaran_api, rep_pemasukan_api)

with open("VPS/server.js", "w") as f:
    f.write(content)
