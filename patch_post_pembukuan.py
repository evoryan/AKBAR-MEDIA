import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """app.post('/api/pembukuan', async (req, res) => {
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
        res.json({ message: "Pembukuan ditambahkan" });"""

rep = """app.post('/api/pembukuan', async (req, res) => {
    try {
        const { type, amount, description, category } = req.body;
        const cat = category || 'Lain-lain';
        try {
            await req.pool.query(
                'INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)',
                [type, amount || 0, description || '', cat]
            );
        } catch (e) {
            await req.pool.query(
                'INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)',
                [type, amount || 0, description || '']
            );
        }
        
        // Update summary table
        if (type === 'pemasukan') {
            await req.pool.query(
                'INSERT INTO pemasukan (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)',
                [cat, amount || 0, description || '']
            ).catch(e => console.error(e));
        } else if (type === 'pengeluaran') {
            await req.pool.query(
                'INSERT INTO pengeluaran (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)',
                [cat, amount || 0, description || '']
            ).catch(e => console.error(e));
        }

        res.json({ message: "Pembukuan ditambahkan" });"""

content = content.replace(target, rep)
with open("VPS/server.js", "w") as f:
    f.write(content)
