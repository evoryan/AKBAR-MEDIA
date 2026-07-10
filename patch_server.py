with open('VPS/server.js', 'r') as f:
    content = f.read()

endpoints = """
app.get('/api/pengeluaran', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT category, amount, description, updated_at FROM pengeluaran');
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.post('/api/pengeluaran', async (req, res) => {
    try {
        const { category, amount, description } = req.body;
        await pool.query(
            'INSERT INTO pengeluaran (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = VALUES(amount), description = VALUES(description)',
            [category, amount || 0, description || '']
        );
        res.json({ message: "Pengeluaran diupdate" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

"""

# Insert right before app.get('/api/areas'
content = content.replace("app.get('/api/areas'", endpoints + "app.get('/api/areas'")

# Note: The total "pengeluaran" in PembukuanScreen still needs to sum these up, OR we can fetch them separately in the frontend.
with open('VPS/server.js', 'w') as f:
    f.write(content)
