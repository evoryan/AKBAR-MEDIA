import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """app.delete('/api/pembukuan/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM pembukuan WHERE id = ?', [req.params.id]);
        res.json({ message: "Pembukuan dihapus" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

rep = """app.delete('/api/pembukuan/:id', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM pembukuan WHERE id = ?', [req.params.id]);
        if (rows.length > 0) {
            const oldRow = rows[0];
            const oldAmount = oldRow.amount || 0;
            const oldCategory = oldRow.category || 'Lain-lain';
            
            // Adjust summary tables
            if (oldRow.type === 'pemasukan') {
                await req.pool.query('UPDATE pemasukan SET amount = amount - ? WHERE category = ?', [oldAmount, oldCategory]).catch(e => console.error(e));
            } else if (oldRow.type === 'pengeluaran') {
                await req.pool.query('UPDATE pengeluaran SET amount = amount - ? WHERE category = ?', [oldAmount, oldCategory]).catch(e => console.error(e));
            }

            // Revert tagihan if it's a payment
            if (oldRow.description && oldRow.description.startsWith('Pembayaran tagihan pelanggan')) {
                const match = oldRow.description.match(/Pembayaran tagihan pelanggan (.*?) \((.*?) (\d+)\)/);
                if (match) {
                    const custName = match[1];
                    const bulan = match[2];
                    const tahun = match[3];
                    const [custs] = await req.pool.query('SELECT id FROM customers WHERE name = ?', [custName]);
                    if (custs.length > 0) {
                        await req.pool.query('UPDATE tagihan_bulanan SET status = "BELUM BAYAR", admin_name = NULL WHERE customer_id = ? AND bulan = ? AND tahun = ?', [custs[0].id, bulan, tahun]).catch(e => console.error(e));
                        await req.pool.query('UPDATE customers SET status = "BELUM BAYAR" WHERE id = ?', [custs[0].id]).catch(e => console.error(e));
                    }
                }
            }
        }
        await req.pool.query('DELETE FROM pembukuan WHERE id = ?', [req.params.id]);
        res.json({ message: "Pembukuan dihapus dan data terkait disesuaikan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""
content = content.replace(target, rep)

target_put = """app.put('/api/pembukuan/:id', async (req, res) => {
    try {
        await req.pool.query("ALTER TABLE pembukuan MODIFY COLUMN type VARCHAR(50)").catch(e => {});
        const { type, category, amount, description } = req.body;
        await req.pool.query(
            'UPDATE pembukuan SET type = ?, category = ?, amount = ?, description = ? WHERE id = ?',
            [type, category || 'Lain-lain', amount, description, req.params.id]
        );
        res.json({ message: "Pembukuan diperbarui" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

rep_put = """app.put('/api/pembukuan/:id', async (req, res) => {
    try {
        await req.pool.query("ALTER TABLE pembukuan MODIFY COLUMN type VARCHAR(50)").catch(e => {});
        const { type, category, amount, description } = req.body;
        const newCategory = category || 'Lain-lain';
        
        const [rows] = await req.pool.query('SELECT * FROM pembukuan WHERE id = ?', [req.params.id]);
        if (rows.length > 0) {
            const oldRow = rows[0];
            const oldAmount = oldRow.amount || 0;
            const oldCategory = oldRow.category || 'Lain-lain';
            
            if (oldRow.type === 'pemasukan') {
                await req.pool.query('UPDATE pemasukan SET amount = amount - ? WHERE category = ?', [oldAmount, oldCategory]).catch(e => console.error(e));
            } else if (oldRow.type === 'pengeluaran') {
                await req.pool.query('UPDATE pengeluaran SET amount = amount - ? WHERE category = ?', [oldAmount, oldCategory]).catch(e => console.error(e));
            }
            
            if (type === 'pemasukan') {
                await req.pool.query('INSERT INTO pemasukan (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)', [newCategory, amount, description]).catch(e => console.error(e));
            } else if (type === 'pengeluaran') {
                await req.pool.query('INSERT INTO pengeluaran (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)', [newCategory, amount, description]).catch(e => console.error(e));
            }
            
            // Note: If description changes significantly, we don't try to automatically re-link tagihan here to avoid complex edge cases.
        }

        await req.pool.query(
            'UPDATE pembukuan SET type = ?, category = ?, amount = ?, description = ? WHERE id = ?',
            [type, newCategory, amount, description, req.params.id]
        );
        res.json({ message: "Pembukuan diperbarui dan data terkait disesuaikan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

content = content.replace(target_put, rep_put)

with open("VPS/server.js", "w") as f:
    f.write(content)

