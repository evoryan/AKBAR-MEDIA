import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

endpoint = """app.get('/api/customers/:id/history', async (req, res) => {
    try {
        const { id } = req.params;
        const [customers] = await req.pool.query('SELECT name FROM customers WHERE id = ?', [id]);
        if (customers.length === 0) return res.json([]);
        const name = customers[0].name;
        const [rows] = await req.pool.query('SELECT * FROM pembukuan WHERE description LIKE ? ORDER BY created_at DESC', [`%${name}%`]);
        res.json(rows);
    } catch(e) {
        res.status(500).json({ error: e.message });
    }
});

app.put('/api/customers/:id', async (req, res) => {"""

content = content.replace("app.put('/api/customers/:id', async (req, res) => {", endpoint)

with open('VPS/server.js', 'w') as f:
    f.write(content)
