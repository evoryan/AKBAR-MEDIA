import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

old_func = """app.post('/api/billing/pay', async (req, res) => {
    try {
        const { customerId, adminName, totalAmount } = req.body;
        
        // Get customer name
        const [customers] = await req.pool.query('SELECT name FROM customers WHERE id = ?', [customerId]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customerName = customers[0].name;

        // Update customer status to LUNAS CASH
        await req.pool.query('UPDATE customers SET status = "LUNAS CASH" WHERE id = ?', [customerId]);

        // Add to pembukuan
        await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
            ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`, 'Transaksi Cash']);

        // Add notification
        const notifMsg = `Pembayaran "${customerName}" di terima oleh "${adminName}"`;
        await req.pool.query('INSERT INTO notifications (message) VALUES (?)', [notifMsg]);

        res.json({ message: "Pembayaran berhasil dicatat" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

new_func = """app.post('/api/billing/pay', async (req, res) => {
    try {
        const { customerId, adminName, totalAmount } = req.body;
        
        // Get customer name
        const [customers] = await req.pool.query('SELECT name FROM customers WHERE id = ?', [customerId]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customerName = customers[0].name;

        // Update customer status to LUNAS CASH
        await req.pool.query('UPDATE customers SET status = "LUNAS CASH" WHERE id = ?', [customerId]);

        // Add to pembukuan
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`, 'Transaksi Cash']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`]);
        }

        // Add notification
        const notifMsg = `Pembayaran "${customerName}" di terima oleh "${adminName}"`;
        try {
            await req.pool.query('INSERT INTO notifications (message) VALUES (?)', [notifMsg]);
        } catch (e) {
            console.error("Warning: notifications table might be missing", e.message);
        }

        res.json({ message: "Pembayaran berhasil dicatat" });
    } catch (error) {
        console.error("Payment API Error:", error);
        res.status(500).json({ error: "Terjadi kesalahan server: " + error.message });
    }
});"""

if old_func in content:
    content = content.replace(old_func, new_func)
else:
    print("Could not find the exact old_func match")

with open('VPS/server.js', 'w') as f:
    f.write(content)
