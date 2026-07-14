import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# 1. Add node-cron require if not there
if "const cron = require('node-cron');" not in content:
    content = content.replace("const express = require('express');", "const express = require('express');\nconst cron = require('node-cron');")

# 2. Add tagihan_bulanan to updateSchema()
target_schema = """        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{}); await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        console.log("Schema checked/updated.");"""

rep_schema = """        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{}); await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        
        await masterPool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            customer_id INT,
            bulan VARCHAR(50),
            tahun INT,
            amount DECIMAL(15, 2),
            status VARCHAR(50) DEFAULT 'BELUM BAYAR',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
        )`).catch(e=>{});
        
        console.log("Schema checked/updated.");"""

if "tagihan_bulanan" not in content:
    content = content.replace(target_schema, rep_schema)

# 3. Replace payBilling
target_pay = """app.post('/api/billing/pay', async (req, res) => {
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

rep_pay = """app.post('/api/billing/pay', async (req, res) => {
    try {
        const { customerId, adminName, totalAmount } = req.body;
        
        // Get customer name
        const [customers] = await req.pool.query('SELECT name FROM customers WHERE id = ?', [customerId]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customerName = customers[0].name;
        
        // Ensure table exists
        await req.pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            customer_id INT,
            bulan VARCHAR(50),
            tahun INT,
            amount DECIMAL(15, 2),
            status VARCHAR(50) DEFAULT 'BELUM BAYAR',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
        )`).catch(e=>{});

        // Mark tagihan as LUNAS CASH
        const [tagihan] = await req.pool.query('SELECT id, bulan, tahun FROM tagihan_bulanan WHERE customer_id = ? AND status = "BELUM BAYAR" ORDER BY id ASC LIMIT 1', [customerId]);
        let desc = `Pembayaran tagihan pelanggan ${customerName}`;
        if (tagihan.length > 0) {
            await req.pool.query('UPDATE tagihan_bulanan SET status = "LUNAS CASH" WHERE id = ?', [tagihan[0].id]);
            desc = `Pembayaran tagihan pelanggan ${customerName} (${tagihan[0].bulan} ${tagihan[0].tahun})`;
        }

        // Update customer status to LUNAS CASH
        await req.pool.query('UPDATE customers SET status = "LUNAS CASH" WHERE id = ?', [customerId]);

        // Add to pembukuan
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc, 'Transaksi Cash']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc]);
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

content = content.replace(target_pay, rep_pay)

# 4. Replace deleteBilling
target_del = """app.post('/api/billing/delete', async (req, res) => {
    try {
        const { customerId } = req.body;
        // The user wants to remove from "Belum Bayar" list. 
        // We can just update the status to "NONAKTIF" or something that isn't "BELUM BAYAR"
        await req.pool.query('UPDATE customers SET status = "TERHAPUS" WHERE id = ?', [customerId]);
        res.json({ message: "Tagihan dihapus" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

rep_del = """app.post('/api/billing/delete', async (req, res) => {
    try {
        const { customerId } = req.body;
        // Get customer name
        const [customers] = await req.pool.query('SELECT name FROM customers WHERE id = ?', [customerId]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customerName = customers[0].name;
        
        // Ensure table exists
        await req.pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            customer_id INT,
            bulan VARCHAR(50),
            tahun INT,
            amount DECIMAL(15, 2),
            status VARCHAR(50) DEFAULT 'BELUM BAYAR',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
        )`).catch(e=>{});

        // Find last paid bill
        const [tagihan] = await req.pool.query('SELECT id, amount, bulan, tahun FROM tagihan_bulanan WHERE customer_id = ? AND status = "LUNAS CASH" ORDER BY id DESC LIMIT 1', [customerId]);
        let refundAmount = 0;
        let desc = `Pembatalan pembayaran pelanggan ${customerName}`;
        
        if (tagihan.length > 0) {
            await req.pool.query('UPDATE tagihan_bulanan SET status = "BELUM BAYAR" WHERE id = ?', [tagihan[0].id]);
            refundAmount = tagihan[0].amount;
            desc = `Pembatalan pembayaran tagihan pelanggan ${customerName} (${tagihan[0].bulan} ${tagihan[0].tahun})`;
        } else {
            // fallback amount if no tagihan_bulanan found
            const [pembukuan] = await req.pool.query('SELECT amount FROM pembukuan WHERE type = "pemasukan" AND description LIKE ? ORDER BY id DESC LIMIT 1', [`%${customerName}%`]);
            if (pembukuan.length > 0) refundAmount = pembukuan[0].amount;
        }

        // Revert customer status
        await req.pool.query('UPDATE customers SET status = "BELUM BAYAR" WHERE id = ?', [customerId]);

        // Add to pembukuan as pengeluaran (refund)
        try {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
                ['pengeluaran', refundAmount, desc, 'Pengembalian Dana']);
        } catch (e) {
            await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
                ['pengeluaran', refundAmount, desc]);
        }

        res.json({ message: "Pembatalan berhasil dan dana dicatat di pembukuan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});"""

content = content.replace(target_del, rep_del)


# 5. Add Cron setup at the end
cron_script = """
cron.schedule('1 0 * * *', async () => {
    console.log('Menjalankan cron job tagihan bulanan...');
    try {
        const [users] = await masterPool.query('SELECT DISTINCT db_name FROM users WHERE db_name IS NOT NULL AND db_name != ""');
        const today = new Date();
        const dateStr = today.getDate().toString();
        const monthNames = ["Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"];
        const currentMonth = monthNames[today.getMonth()];
        const currentYear = today.getFullYear();
        
        for (const user of users) {
            const dbName = user.db_name;
            const pool = getTenantPool(dbName);
            try {
                // Ensure table exists
                await pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    customer_id INT,
                    bulan VARCHAR(50),
                    tahun INT,
                    amount DECIMAL(15, 2),
                    status VARCHAR(50) DEFAULT 'BELUM BAYAR',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
                )`);
                
                const [customers] = await pool.query('SELECT * FROM customers WHERE billingDate = ? AND status != "TERHAPUS"', [dateStr]);
                for (const customer of customers) {
                    const [existing] = await pool.query(
                        'SELECT id FROM tagihan_bulanan WHERE customer_id = ? AND bulan = ? AND tahun = ?',
                        [customer.id, currentMonth, currentYear]
                    );
                    if (existing.length === 0) {
                        let amount = 0;
                        if (customer.price) {
                            amount = parseFloat(customer.price.replace(/[^0-9]/g, '')) || 0;
                        }
                        await pool.query(
                            'INSERT INTO tagihan_bulanan (customer_id, bulan, tahun, amount, status) VALUES (?, ?, ?, ?, "BELUM BAYAR")',
                            [customer.id, currentMonth, currentYear, amount]
                        );
                        await pool.query('UPDATE customers SET status = "BELUM BAYAR" WHERE id = ?', [customer.id]);
                        console.log(`Tagihan dibuat untuk ${customer.name} di database ${dbName}`);
                    }
                }
            } catch (e) {
                console.error(`Error processing db ${dbName}:`, e.message);
            }
        }
    } catch(e) {
        console.error('Cron job error:', e);
    }
});
"""
if "cron.schedule" not in content:
    content = content.replace("app.listen(PORT", cron_script + "\napp.listen(PORT")

with open("VPS/server.js", "w") as f:
    f.write(content)
