import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """        // Add to pembukuan as pengeluaran (refund)
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                ['pengeluaran', refundAmount, desc, 'Pengembalian Dana']);
        } catch (e) {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                ['pengeluaran', refundAmount, desc]);
        }"""

rep = """        // Add to pembukuan as pengeluaran (refund)
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                ['pengeluaran', refundAmount, desc, 'Pengembalian Dana']);
        } catch (e) {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                ['pengeluaran', refundAmount, desc]);
        }
        
        // Also add to pengeluaran summary table
        try {
            await req.pool.query(
                'INSERT INTO pengeluaran (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)',
                ['Pengembalian Dana', refundAmount, desc]
            );
        } catch (e) {
            console.error("Warning: failed to insert to pengeluaran table", e.message);
        }"""

content = content.replace(target, rep)

with open("VPS/server.js", "w") as f:
    f.write(content)
