import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Add endpoints for Pembukuan
pembukuan_endpoints = """
app.get('/api/pembukuan/all', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM pembukuan ORDER BY id DESC');
        res.json(rows.map(r => ({ ...r, id: r.id.toString(), amount: Number(r.amount) })));
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.put('/api/pembukuan/:id', async (req, res) => {
    try {
        const { type, category, amount, description } = req.body;
        await req.pool.query(
            'UPDATE pembukuan SET type = ?, category = ?, amount = ?, description = ? WHERE id = ?',
            [type, category || 'Lain-lain', amount, description, req.params.id]
        );
        res.json({ message: "Pembukuan diperbarui" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.delete('/api/pembukuan/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM pembukuan WHERE id = ?', [req.params.id]);
        res.json({ message: "Pembukuan dihapus" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.post('/api/setoran', async (req, res) => {
    try {
        const { adminName, amount } = req.body;
        await req.pool.query('ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)').catch(e=>{});
        await req.pool.query(
            'INSERT INTO pembukuan (type, amount, description, category, admin_name) VALUES (?, ?, ?, ?, ?)',
            ['setor', amount, `Setoran oleh ${adminName}`, 'Setoran', adminName]
        );
        res.json({ message: "Setoran berhasil ditambahkan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

"""

if "app.get('/api/pembukuan/all'" not in content:
    target = "app.post('/api/pembukuan'"
    content = content.replace(target, pembukuan_endpoints + "\n" + target)

# Modify `/api/uang-di-admin`
uang_target = """app.get('/api/uang-di-admin', async (req, res) => {
    try {
        const [rows] = await req.pool.query(`
            SELECT admin_name as adminName, SUM(amount) as totalAmount, COUNT(*) as jmlPlggn 
            FROM pembukuan 
            WHERE category = 'Transaksi Cash' AND type = 'pemasukan' 
            GROUP BY admin_name
        `);
        res.json(rows);"""

uang_rep = """app.get('/api/uang-di-admin', async (req, res) => {
    try {
        const [pemasukan] = await req.pool.query(`
            SELECT admin_name as adminName, SUM(amount) as totalAmount, COUNT(*) as jmlPlggn 
            FROM pembukuan 
            WHERE type = 'pemasukan' AND admin_name IS NOT NULL
            GROUP BY admin_name
        `);
        const [setoran] = await req.pool.query(`
            SELECT admin_name as adminName, SUM(amount) as totalAmount
            FROM pembukuan 
            WHERE type = 'setor' AND admin_name IS NOT NULL
            GROUP BY admin_name
        `);
        const [pengeluaran] = await req.pool.query(`
            SELECT admin_name as adminName, SUM(amount) as totalAmount
            FROM pembukuan 
            WHERE type = 'pengeluaran' AND admin_name IS NOT NULL
            GROUP BY admin_name
        `);
        
        let result = {};
        pemasukan.forEach(row => {
            result[row.adminName] = { 
                adminName: row.adminName, 
                totalDiterima: Number(row.totalAmount), 
                jmlPlggn: row.jmlPlggn,
                setor: 0,
                pengeluaran: 0
            };
        });
        setoran.forEach(row => {
            if (!result[row.adminName]) result[row.adminName] = { adminName: row.adminName, totalDiterima: 0, jmlPlggn: 0, setor: 0, pengeluaran: 0 };
            result[row.adminName].setor = Number(row.totalAmount);
        });
        pengeluaran.forEach(row => {
            if (!result[row.adminName]) result[row.adminName] = { adminName: row.adminName, totalDiterima: 0, jmlPlggn: 0, setor: 0, pengeluaran: 0 };
            result[row.adminName].pengeluaran = Number(row.totalAmount);
        });
        
        res.json(Object.values(result));"""

content = content.replace(uang_target, uang_rep)

# In `/api/billing/pay`, ensure we set `admin_name`.
pay_target = """        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc, 'Transaksi Cash']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc]);
        }"""

pay_rep = """        try {
            await req.pool.query('ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)').catch(e=>{});
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category, admin_name) VALUES (?, ?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc, 'Transaksi Cash', adminName || 'Admin']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, admin_name) VALUES (?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc, adminName || 'Admin']);
        }"""

content = content.replace(pay_target, pay_rep)


with open("VPS/server.js", "w") as f:
    f.write(content)

