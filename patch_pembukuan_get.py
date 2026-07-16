import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """        // Also fetch from pengeluaran table
        try {
            const [pengeluaranRows] = await req.pool.query('SELECT category, amount FROM pengeluaran');
            pengeluaranRows.forEach(row => {
                summary.pengeluaran += Number(row.amount);
                summary.categories[row.category] = (summary.categories[row.category] || 0) + Number(row.amount);
            });
        } catch(e) {
            console.log("pengeluaran table might not exist yet");
        }"""

rep = """        // Fetch from pengeluaran table removed to prevent double-counting
        // Since all pengeluaran items are now stored in pembukuan table"""

content = content.replace(target, rep)

with open("VPS/server.js", "w") as f:
    f.write(content)

