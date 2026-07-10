with open('VPS/server.js', 'r') as f:
    content = f.read()

get_pembukuan_replacement = """
app.get('/api/pembukuan', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT type, category, SUM(amount) as total FROM pembukuan GROUP BY type, category');
        let summary = {
            pemasukan: 0,
            pengeluaran: 0,
            categories: {}
        };
        rows.forEach(row => {
            if (row.type === 'pemasukan') summary.pemasukan += Number(row.total);
            if (row.type === 'pengeluaran') summary.pengeluaran += Number(row.total);
            summary.categories[row.category || 'Lain-lain'] = Number(row.total);
        });
        
        // Also fetch from pengeluaran table
        try {
            const [pengeluaranRows] = await pool.query('SELECT category, amount FROM pengeluaran');
            pengeluaranRows.forEach(row => {
                summary.pengeluaran += Number(row.amount);
                summary.categories[row.category] = (summary.categories[row.category] || 0) + Number(row.amount);
            });
        } catch(e) {
            console.log("pengeluaran table might not exist yet");
        }

        res.json(summary);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});
"""

import re
# Replace the whole app.get('/api/pembukuan') block
content = re.sub(r"app\.get\('/api/pembukuan', async \(req, res\) => \{.*?(?=\napp\.post\('/api/pembukuan')", get_pembukuan_replacement, content, flags=re.DOTALL)

with open('VPS/server.js', 'w') as f:
    f.write(content)
