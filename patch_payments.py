with open('VPS/server.js', 'r') as f:
    content = f.read()

payment_endpoint = """
app.post('/api/billing/pay', async (req, res) => {
    try {
        const { customerId, adminName, totalAmount } = req.body;
        
        // Get customer name
        const [customers] = await pool.query('SELECT name FROM customers WHERE id = ?', [customerId]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customerName = customers[0].name;

        // Update customer status to LUNAS CASH
        await pool.query('UPDATE customers SET status = "LUNAS CASH" WHERE id = ?', [customerId]);

        // Add to pembukuan
        await pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
            ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`]);

        // Add notification
        const notifMsg = `Pembayaran "${customerName}" di terima oleh "${adminName}"`;
        await pool.query('INSERT INTO notifications (message) VALUES (?)', [notifMsg]);

        res.json({ message: "Pembayaran berhasil dicatat" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/notifications', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM notifications ORDER BY created_at DESC LIMIT 50');
        res.json(rows.map(r => ({ ...r, id: r.id.toString() })));
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.post('/api/billing/delete', async (req, res) => {
    try {
        const { customerId } = req.body;
        // The user wants to remove from "Belum Bayar" list. 
        // We can just update the status to "NONAKTIF" or something that isn't "BELUM BAYAR"
        await pool.query('UPDATE customers SET status = "TERHAPUS" WHERE id = ?', [customerId]);
        res.json({ message: "Tagihan dihapus" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});
"""

if "/api/billing/pay" not in content:
    content = content.replace("// Endpoints", "// Endpoints\n" + payment_endpoint)
    with open('VPS/server.js', 'w') as f:
        f.write(content)
