with open('VPS/server.js', 'r') as f:
    content = f.read()

dashboard_endpoint = """
app.get('/api/dashboard/summary', async (req, res) => {
    try {
        const [customers] = await pool.query('SELECT COUNT(*) as total FROM customers');
        const [paidCustomers] = await pool.query('SELECT COUNT(*) as paid FROM customers WHERE status = "Aktif"');
        const [unpaidCustomers] = await pool.query('SELECT COUNT(*) as unpaid FROM customers WHERE status != "Aktif"');
        const [pembukuan] = await pool.query('SELECT type, SUM(amount) as total FROM pembukuan GROUP BY type');
        
        let monthlyRevenue = 0;
        pembukuan.forEach(row => {
            if (row.type === 'pemasukan') monthlyRevenue += Number(row.total);
        });

        res.json({
            totalCustomers: customers[0].total,
            monthlyRevenue: monthlyRevenue,
            activePPPoE: customers[0].total, // simplified
            activeHotspot: 0,
            paidCustomers: paidCustomers[0].paid,
            unpaidCustomers: unpaidCustomers[0].unpaid
        });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});
"""

if "/api/dashboard/summary" not in content:
    content = content.replace("// Endpoints", "// Endpoints\n" + dashboard_endpoint)
    with open('VPS/server.js', 'w') as f:
        f.write(content)

