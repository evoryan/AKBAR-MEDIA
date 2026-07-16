import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# insert before app.delete('/api/customers/:id'
pattern = r"app\.delete\('/api/customers/:id'"
replacement = """app.put('/api/customers/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, phone, area, address, username, billingDate, status, price, discount, additionalCost1, additionalCost2 } = req.body;
        const registerDate = req.body.registerDate || req.body.register_date;
        const isolateDate = req.body.isolateDate || req.body.isolate_date;
        const packageName = req.body.packageName || req.body.package_name;
        const pppoeSecret = req.body.pppoeSecret || req.body.pppoe_secret;
        const odpId = req.body.odpId || req.body.odp_id;
        const odpPort = req.body.odpPort || req.body.odp_port;
        
        const parsedOdpId = (odpId !== undefined && odpId !== null && odpId !== '') ? parseInt(odpId) : null;
        
        await req.pool.query(
            'UPDATE customers SET name=?, phone=?, area=?, address=?, username=?, billingDate=?, status=?, price=?, discount=?, register_date=?, isolate_date=?, package_name=?, pppoe_secret=?, odp_id=?, odp_port=?, additionalCost1=?, additionalCost2=? WHERE id=?',
            [name, phone, area, address || '', username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', parsedOdpId, odpPort || '', additionalCost1 || '', additionalCost2 || '', id]
        );
        res.json({ message: "Pelanggan berhasil diupdate" });
    } catch (error) {
        console.error("API Error (update customer):", error.message);
        res.status(500).json({ error: error.message || "Terjadi kesalahan saat update pelanggan" });
    }
});

app.delete('/api/customers/:id'"""

content = re.sub(pattern, replacement, content)

with open("VPS/server.js", "w") as f:
    f.write(content)

