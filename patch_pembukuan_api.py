with open('VPS/server.js', 'r') as f:
    content = f.read()

old_pembukuan = """app.get('/api/pembukuan', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT type, SUM(amount) as total FROM pembukuan GROUP BY type');
        let pemasukan = 0;
        let pengeluaran = 0;
        rows.forEach(row => {
            if (row.type === 'pemasukan') pemasukan = Number(row.total);
            if (row.type === 'pengeluaran') pengeluaran = Number(row.total);
        });
        res.json({ pemasukan, pengeluaran });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

new_pembukuan = """app.get('/api/pembukuan', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT type, category, SUM(amount) as total FROM pembukuan GROUP BY type, category');
        let summary = {
            pemasukan: 0,
            pengeluaran: 0,
            categories: {}
        };
        rows.forEach(row => {
            if (row.type === 'pemasukan') summary.pemasukan += Number(row.total);
            if (row.type === 'pengeluaran') summary.pengeluaran += Number(row.total);
            summary.categories[row.category || 'Lain-lain'] = Number(row.total);
        });
        res.json(summary);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.post('/api/pembukuan', async (req, res) => {
    try {
        const { type, amount, description, category } = req.body;
        await pool.query(
            'INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)',
            [type, amount || 0, description || '', category || 'Lain-lain']
        );
        res.json({ message: "Pembukuan ditambahkan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

if "summary.categories" not in content:
    content = content.replace(old_pembukuan, new_pembukuan)
    with open('VPS/server.js', 'w') as f:
        f.write(content)
