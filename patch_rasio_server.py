import re

with open('server_updated.js', 'r') as f:
    content = f.read()

# Add table creation
table_creation = """            await tPool.query(`CREATE TABLE IF NOT EXISTS rasio (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                location VARCHAR(255),
                size VARCHAR(255),
                redaman_in VARCHAR(50),
                redaman_out_a VARCHAR(50),
                redaman_out_b VARCHAR(50)
            )`).catch(e=>{});"""

if "CREATE TABLE IF NOT EXISTS rasio" not in content:
    content = content.replace("await tPool.query(`CREATE TABLE IF NOT EXISTS odc_list", table_creation + "\n            await tPool.query(`CREATE TABLE IF NOT EXISTS odc_list")

# Add routes
routes = """
app.get('/api/rasio', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM rasio');
        const rasioList = rows.map(r => ({ ...r, id: r.id.toString() }));
        res.json(rasioList);
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan server" });
    }
});

app.post('/api/rasio', async (req, res) => {
    try {
        const { name, location, size, redaman_in, redaman_out_a, redaman_out_b } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO rasio (name, location, size, redaman_in, redaman_out_a, redaman_out_b) VALUES (?, ?, ?, ?, ?, ?)',
            [name, location || '', size || '', redaman_in || '', redaman_out_a || '', redaman_out_b || '']
        );
        res.json({ message: "Rasio ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan server" });
    }
});

app.put('/api/rasio/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, location, size, redaman_in, redaman_out_a, redaman_out_b } = req.body;
        await req.pool.query(
            'UPDATE rasio SET name = ?, location = ?, size = ?, redaman_in = ?, redaman_out_a = ?, redaman_out_b = ? WHERE id = ?',
            [name, location || '', size || '', redaman_in || '', redaman_out_a || '', redaman_out_b || '', id]
        );
        res.json({ message: "Rasio diupdate" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan server" });
    }
});

app.delete('/api/rasio/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM rasio WHERE id = ?', [req.params.id]);
        res.json({ message: "Rasio berhasil dihapus" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan server" });
    }
});
"""

if "app.get('/api/rasio'" not in content:
    content = content.replace("app.get('/api/odc'", routes + "\napp.get('/api/odc'")

with open('server_updated.js', 'w') as f:
    f.write(content)
