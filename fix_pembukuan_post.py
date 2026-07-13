import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

old_post = """app.post('/api/pembukuan', async (req, res) => {
    try {
        const { type, amount, description, category } = req.body;
        await req.pool.query(
            'INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)',
            [type, amount || 0, description || '', category || 'Lain-lain']
        );
        res.json({ message: "Pembukuan ditambahkan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

new_post = """app.post('/api/pembukuan', async (req, res) => {
    try {
        const { type, amount, description, category } = req.body;
        try {
            await req.pool.query(
                'INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)',
                [type, amount || 0, description || '', category || 'Lain-lain']
            );
        } catch (e) {
            await req.pool.query(
                'INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)',
                [type, amount || 0, description || '']
            );
        }
        res.json({ message: "Pembukuan ditambahkan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

if old_post in content:
    content = content.replace(old_post, new_post)

with open('VPS/server.js', 'w') as f:
    f.write(content)
