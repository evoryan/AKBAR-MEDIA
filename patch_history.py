import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

history_endpoint = """
app.get('/api/customers/:id/history', async (req, res) => {
    try {
        const [customers] = await req.pool.query('SELECT name FROM customers WHERE id = ?', [req.params.id]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customerName = customers[0].name;
        const [rows] = await req.pool.query('SELECT * FROM pembukuan WHERE description LIKE ? ORDER BY created_at DESC', [`%${customerName}%`]);
        const history = rows.map(r => ({ ...r, id: r.id.toString(), amount: r.amount.toString() }));
        res.json(history);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});
"""

# Insert it before app.delete('/api/customers/:id'
content = content.replace("app.delete('/api/customers/:id'", history_endpoint + "\napp.delete('/api/customers/:id'")

with open('VPS/server.js', 'w') as f:
    f.write(content)
