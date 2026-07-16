import re

with open("VPS/server.js", "r") as f:
    content = f.read()

replacement = """        let totalGlobalRevenue = 0;
        try {
            const [custRows] = await req.pool.query('SELECT price FROM customers WHERE status IS NULL OR status != "TERHAPUS"');
            custRows.forEach(row => {
                if (row.price) {
                    const priceStr = String(row.price).replace(/\\.0$/, '').replace(/[^0-9]/g, '');
                    const priceVal = parseInt(priceStr);
                    if (!isNaN(priceVal)) totalGlobalRevenue += priceVal;
                }
            });
        } catch(e) {
            console.error("Error calculating global revenue", e);
        }

        res.json({
            totalCustomers: customers[0].total,
            monthlyRevenue: monthlyRevenue,
            totalGlobalRevenue: totalGlobalRevenue,
            activePPPoE: customers[0].total,
            activeHotspot: 0,
            paidCustomers: paidCustomers[0].paid,
            unpaidCustomers: unpaidCustomers[0].unpaid
        });"""

content = re.sub(
    r"res\.json\(\{\s*totalCustomers: customers\[0\]\.total,\s*monthlyRevenue: monthlyRevenue,\s*activePPPoE: customers\[0\]\.total, // simplified\s*activeHotspot: 0,\s*paidCustomers: paidCustomers\[0\]\.paid,\s*unpaidCustomers: unpaidCustomers\[0\]\.unpaid\s*\}\);",
    replacement, content
)

with open("VPS/server.js", "w") as f:
    f.write(content)
