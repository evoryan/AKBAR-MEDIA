import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

new_customer_endpoint = """app.post('/api/customers', async (req, res) => {
    try {
        const { name, phone, area, username, billingDate, status, price, discount, registerDate, isolateDate, packageName, additionalCost1, additionalCost2, pppoeSecret, odpId, odpPort } = req.body;
        
        const [result] = await req.pool.query(
            'INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [name, phone, area, username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', odpId || '', odpPort || '']
        );
        
        // Handle additional costs
        const addCost1 = parseFloat(additionalCost1) || 0;
        const addCost2 = parseFloat(additionalCost2) || 0;
        
        if (addCost1 > 0 || addCost2 > 0) {
            const totalCost = addCost1 + addCost2;
            const description = `Biaya tambahan pendaftaran ${name}`;
            await req.pool.query(
                'INSERT INTO pembukuan (type, category, amount, description) VALUES (?, ?, ?, ?)',
                ['pemasukan', 'Pemasukan Lain-lain', totalCost, description]
            );
        }

        res.json({ message: "Pelanggan berhasil ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("Error adding customer:", error);
        res.status(500).json({ error: "Terjadi kesalahan saat menambahkan pelanggan" });
    }
});"""

content = re.sub(
    r"app\.post\('/api/customers', async \(req, res\) => \{.*?\n\}\);" ,
    new_customer_endpoint,
    content,
    flags=re.DOTALL
)

with open('VPS/server.js', 'w') as f:
    f.write(content)
