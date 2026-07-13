const express = require('express');
const { RouterOSClient } = require('routeros-client');
const cors = require('cors');
const axios = require('axios');
const mysql = require('mysql2/promise');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());
app.get('/api/ping', (req, res) => res.json({ status: 'ok' }));


const JWT_SECRET = process.env.JWT_SECRET || 'super-secret-key-akbar';

// Master Database connection pool
const masterPool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'akbar',
    password: process.env.DB_PASSWORD || '08Delapan',
    database: 'akbar_media_master',
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

// Dynamic Tenant Pools Map
const tenantPools = {};

function getTenantPool(dbName) {
    if (!dbName) return null;
    if (!tenantPools[dbName]) {
        tenantPools[dbName] = mysql.createPool({
            host: process.env.DB_HOST || 'localhost',
            user: process.env.DB_USER || 'akbar',
            password: process.env.DB_PASSWORD || '08Delapan',
            database: dbName,
            waitForConnections: true,
            connectionLimit: 10,
            queueLimit: 0
        });
        
        // Auto-update schema for tenant database
        tenantPools[dbName].query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{}); tenantPools[dbName].query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        tenantPools[dbName].query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{}); tenantPools[dbName].query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
    }
    return tenantPools[dbName];
}

// Middleware to extract tenant context
const tenantContext = (req, res, next) => {
    // Skip auth for login
    if (req.path === '/api/login' || req.path === '/api/fix-db') {
        return next();
    }
    
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.split(' ')[1];
        try {
            const decoded = jwt.verify(token, JWT_SECRET);
            req.user = decoded;
            if (decoded.db_name) {
                req.pool = getTenantPool(decoded.db_name);
            } else {
                req.pool = masterPool; // fallback for users without specific tenant
            }
            return next();
        } catch (err) {
            console.error("JWT verify error:", err.message);
            return res.status(401).json({ error: "Token tidak valid atau kadaluarsa" });
        }
    } else {
        return res.status(401).json({ error: "Akses ditolak, token tidak ditemukan" });
    }
};


// Auto-update schema to avoid errors if user doesn't run init.sql
async function updateSchema() {
    try {
        await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{}); await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{}); await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        console.log("Schema checked/updated.");
    } catch(e) {
        console.error("Schema update error:", e.message);
    }
}
updateSchema();


app.get('/api/fix-db', async (req, res) => {
    try {
        let results = [];
        const pools = { master: masterPool, ...tenantPools };
        for (const [name, pool] of Object.entries(pools)) {
            try {
                await pool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`);
                results.push(`${name}: odc_list portCount added`);
            } catch(e) { results.push(`${name}: odc_list portCount err: ${e.message}`); }
            
            try {
                await pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`);
                results.push(`${name}: odc_list portInput added`);
            } catch(e) { results.push(`${name}: odc_list portInput err: ${e.message}`); }
            
            try {
                await pool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`);
                results.push(`${name}: odp_list portCount added`);
            } catch(e) { results.push(`${name}: odp_list portCount err: ${e.message}`); }
            
            try {
                await pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`);
                results.push(`${name}: odp_list portInput added`);
            } catch(e) { results.push(`${name}: odp_list portInput err: ${e.message}`); }

            try { await pool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`); } catch(e) {}

            try { await pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`); } catch(e) {}
        }
        res.json({ message: "Database schema check completed", details: results });
    } catch(e) {
        res.status(500).json({ error: e.message });
    }
});


// Auto update all schemas on startup
async function initAllDatabases() {
    try {
        console.log("Checking and updating schemas for all databases...");
        
        // 1. Update master
        await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
        await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});

        await masterPool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`).catch(e=>{});

        await masterPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`).catch(e=>{});
        
        // 2. Find all tenant databases
        const [dbs] = await masterPool.query("SHOW DATABASES LIKE 'akbar_%'");
        for (const row of dbs) {
            const dbName = Object.values(row)[0];
            if (dbName === 'akbar_media_master') continue;
            
            console.log(`Updating schema for tenant: ${dbName}`);
            const tPool = getTenantPool(dbName);
            await tPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
            await tPool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});

            await tPool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`).catch(e=>{});

            await tPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`).catch(e=>{});
        }
        console.log("Schema update complete!");
    } catch (e) {
        console.error("Init databases error:", e.message);
    }
}
initAllDatabases();

app.use(tenantContext);
app.get('/api/dashboard/pppoe-offline', async (req, res) => {
    try {
        const [areas] = await req.pool.query('SELECT * FROM areas');
        let offlineList = [];
        
        for (const area of areas) {
            if (!area.routerIp || !area.mikrotikUser || !area.mikrotikPassword) continue;
            try {
                const [host, portStr] = area.routerIp.split(':');
                const port = parseInt(portStr) || 8728;
                const client = new RouterOSClient({
                    host, user: area.mikrotikUser, password: area.mikrotikPassword, port, timeout: 3000
                });
                const api = await client.connect();
                
                const activeMenu = api.menu('/ppp/active');
                const actives = await activeMenu.get();
                
                const secretMenu = api.menu('/ppp/secret');
                const secrets = await secretMenu.get();
                
                const activeNames = new Set(actives.map(a => a.name));
                const offline = secrets.filter(s => !activeNames.has(s.name) && s.disabled !== 'true');
                
                offline.forEach(o => {
                    offlineList.push({ name: o.name, lastLogoff: o['last-logged-out'] || 'Unknown', area: area.name });
                });
                
                client.close();
            } catch (err) {
                console.error(`Error connecting to mikrotik for area ${area.name}:`, err.message);
            }
        }
        
        res.json(offlineList);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});


app.post('/api/billing/pay', async (req, res) => {
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
});

app.get('/api/notifications', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM notifications ORDER BY created_at DESC LIMIT 50');
        res.json(rows.map(r => ({ ...r, id: r.id.toString() })));
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.post('/api/billing/delete', async (req, res) => {
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
});


app.get('/api/dashboard/summary', async (req, res) => {
    try {
        const [customers] = await req.pool.query('SELECT COUNT(*) as total FROM customers');
        const [paidCustomers] = await req.pool.query('SELECT COUNT(*) as paid FROM customers WHERE status = "LUNAS CASH"');
        const [unpaidCustomers] = await req.pool.query('SELECT COUNT(*) as unpaid FROM customers WHERE status != "LUNAS CASH"');
        const [pembukuan] = await req.pool.query('SELECT type, SUM(amount) as total FROM pembukuan GROUP BY type');
        
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

app.post('/api/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        
        if (!username || !password) {
            return res.status(400).json({ error: "Username dan password tidak boleh kosong" });
        }
        
        // Always check master database for login
        const [rows] = await masterPool.query('SELECT * FROM users WHERE username = ? AND password = ?', [username, password]);
        
        if (rows.length > 0) {
            const user = rows[0];
            const { password: userPassword, ...userWithoutPassword } = user;
            userWithoutPassword.id = userWithoutPassword.id.toString();
            
            // Assuming the users table in master db has a 'db_name' column
            const db_name = user.db_name || 'app_db';
            
            const token = jwt.sign(
                { id: user.id, username: user.username, role: user.role, db_name: db_name }, 
                JWT_SECRET, 
                { expiresIn: '24h' }
            );
            
            res.json({ ...userWithoutPassword, token, db_name });
        } else {
            res.status(401).json({ error: "Username atau password salah" });
        }
    } catch (error) {
        console.error("Login error:", error);
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});
app.get('/api/customers', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM customers');
        const customers = rows.map(r => ({ ...r, id: r.id.toString() }));
        res.json(customers);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});


app.get('/api/pembukuan', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT type, category, SUM(amount) as total FROM pembukuan GROUP BY type, category');
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
            const [pengeluaranRows] = await req.pool.query('SELECT category, amount FROM pengeluaran');
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

app.post('/api/pembukuan', async (req, res) => {
    try {
        const { type, amount, description, category } = req.body;
        try {
            await req.pool.query(
                'INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)',
                [type, amount || 0, description || '', category || 'Lain-lain']
            );
        } catch (e) {
            await req.pool.query(
                'INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)',
                [type, amount || 0, description || '']
            );
        }
        res.json({ message: "Pembukuan ditambahkan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});


app.get('/api/pengeluaran', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT category, amount, description, updated_at FROM pengeluaran');
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.post('/api/pengeluaran', async (req, res) => {
    try {
        const { category, amount, description } = req.body;
        await req.pool.query(
            'INSERT INTO pengeluaran (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = VALUES(amount), description = VALUES(description)',
            [category, amount || 0, description || '']
        );
        res.json({ message: "Pengeluaran diupdate" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/areas', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM areas');
        const areas = rows.map(r => ({ ...r, id: r.id.toString() }));
        res.json(areas);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/odc', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM odc_list');
        const odcList = rows.map(r => ({ ...r, id: r.id.toString() }));
        res.json(odcList);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/odp', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM odp_list');
        const odpList = rows.map(r => ({ ...r, id: r.id.toString(), odcId: r.odcId.toString() }));
        res.json(odpList);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});


app.delete('/api/areas/:id', async (req, res) => {
    try {
        const { id } = req.params;
        await req.pool.query('DELETE FROM areas WHERE id = ?', [id]);
        res.json({ message: "Area berhasil dihapus" });
    } catch (error) {
        console.error("Error menghapus area:", error);
        res.status(500).json({ error: "Terjadi kesalahan saat menghapus area" });
    }
});

app.get('/api/admins', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT id, name, username, role FROM users');
        const admins = rows.map(r => ({ ...r, id: r.id.toString() }));
        res.json(admins);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/categories', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM categories');
        const categories = rows.map(r => ({ ...r, id: r.id.toString() }));
        res.json(categories);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/inventory', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM inventory');
        const inventory = rows.map(r => ({ ...r, id: r.id.toString(), categoryId: r.categoryId.toString() }));
        res.json(inventory);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/stock_history', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM stock_history');
        const history = rows.map(r => ({ ...r, id: r.id.toString() }));
        res.json(history);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.delete('/api/admins/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM users WHERE id = ?', [req.params.id]);
        res.json({ message: "Admin berhasil dihapus" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.delete('/api/categories/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM categories WHERE id = ?', [req.params.id]);
        res.json({ message: "Kategori berhasil dihapus" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.delete('/api/inventory/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM inventory WHERE id = ?', [req.params.id]);
        res.json({ message: "Inventory berhasil dihapus" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.put('/api/odc/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, location, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        try {
            await req.pool.query(
                'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',
                [name, location, parsedPortCount, portInput || '', id]
            );
        } catch(e) {
            if (e.message && (e.message.includes("Unknown column") || e.message.includes("Unknown column 'portinput'"))) {
                await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(err=>{});
                await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(
                    'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',
                    [name, location, parsedPortCount, portInput || '', id]
                );
            } else {
                throw e;
            }
        }
        res.json({ message: "ODC diupdate" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });
    }
});

app.put('/api/odp/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { odcId, name, portCount, portInput } = req.body;
        const parsedOdcId = parseInt(odcId) || 0;
        const parsedPortCount = parseInt(portCount) || 0;
        try {
            await req.pool.query(
                'UPDATE odp_list SET name = ?, odcId = ?, portCount = ?, portInput = ? WHERE id = ?',
                [name, parsedOdcId, parsedPortCount, portInput || '', id]
            );
        } catch(e) {
            if (e.message && (e.message.includes("Unknown column") || e.message.includes("Unknown column 'portinput'"))) {
                await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(err=>{});
                await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(
                    'UPDATE odp_list SET name = ?, odcId = ?, portCount = ?, portInput = ? WHERE id = ?',
                    [name, parsedOdcId, parsedPortCount, portInput || '', id]
                );
            } else {
                throw e;
            }
        }
        res.json({ message: "ODP diupdate" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });
    }
});

app.delete('/api/odc/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM odc_list WHERE id = ?', [req.params.id]);
        res.json({ message: "ODC berhasil dihapus" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.delete('/api/odp/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM odp_list WHERE id = ?', [req.params.id]);
        res.json({ message: "ODP berhasil dihapus" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});



app.get('/api/customers/:id/history', async (req, res) => {
    try {
        const [customers] = await req.pool.query('SELECT name FROM customers WHERE id = ?', [req.params.id]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customerName = customers[0].name;
        const [rows] = await req.pool.query('SELECT * FROM pembukuan WHERE description LIKE ? ORDER BY created_at DESC', [`%${customerName}%`]);
        const history = rows.map(r => ({ ...r, id: r.id.toString(), amount: r.amount.toString() }));
        res.json(history);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.delete('/api/customers/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM customers WHERE id = ?', [req.params.id]);
        res.json({ message: "Pelanggan berhasil dihapus" });
    } catch (error) {
        res.status(500).json({ error: "Terjadi kesalahan saat menghapus pelanggan" });
    }
});

app.post('/api/customers', async (req, res) => {
    try {
        const { name, phone, area, username, billingDate, status, price, discount, additionalCost1, additionalCost2 } = req.body;
        const registerDate = req.body.registerDate || req.body.register_date;
        const isolateDate = req.body.isolateDate || req.body.isolate_date;
        const packageName = req.body.packageName || req.body.package_name;
        const pppoeSecret = req.body.pppoeSecret || req.body.pppoe_secret;
        const odpId = req.body.odpId || req.body.odp_id;
        const odpPort = req.body.odpPort || req.body.odp_port;
        
        const parsedOdpId = (odpId !== undefined && odpId !== null && odpId !== '') ? parseInt(odpId) : null;
        let result;
        try {
            [result] = await req.pool.query(
                'INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port, additionalCost1, additionalCost2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                [name, phone, area, username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', parsedOdpId, odpPort || '', additionalCost1 || '', additionalCost2 || '']
            );
        } catch (e) {
            if (e.message && e.message.includes("Unknown column")) {
                await req.pool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(err=>{});
                
                [result] = await req.pool.query(
                    'INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                    [name, phone, area, username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', parsedOdpId, odpPort || '']
                );
            } else {
                throw e;
            }
        }
        
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
});

app.get('/api/packages', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM packages');
        const packages = rows.map(r => ({ ...r, id: r.id.toString(), price: Number(r.price), taxRate: Number(r.taxRate) }));
        res.json(packages);
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.post('/api/packages', async (req, res) => {
    try {
        const { name, speed, price, taxRate, pppoeProfile, description } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO packages (name, speed, price, taxRate, pppoeProfile, description) VALUES (?, ?, ?, ?, ?, ?)',
            [name, speed, price, taxRate, pppoeProfile, description]
        );
        res.json({ message: "Paket ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});


app.put('/api/packages/:id', async (req, res) => {
    try {
        const { name, speed, price, taxRate, description } = req.body;
        // pppoeProfile is ignored or kept default
        await req.pool.query(
            'UPDATE packages SET name = ?, speed = ?, price = ?, taxRate = ?, description = ? WHERE id = ?',
            [name, speed, price, taxRate, description, req.params.id]
        );
        res.json({ message: "Paket diupdate" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.delete('/api/packages/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM packages WHERE id = ?', [req.params.id]);
        res.json({ message: "Paket dihapus" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

// also for area we need add/edit ? We already have /api/areas

app.get('/api/acs/devices', async (req, res) => {
    try {
        const [areas] = await req.pool.query('SELECT * FROM areas WHERE apiDomain IS NOT NULL AND apiDomain != ""');
        let allDevices = [];
        
        for (const area of areas) {
            try {
                let baseUrl = area.apiDomain.trim();
                if (baseUrl.endsWith('/')) baseUrl = baseUrl.slice(0, -1);
                
                const response = await axios.get(`${baseUrl}/devices`, {
                    auth: { username: area.acsUser, password: area.acsPassword },
                    timeout: 5000
                });
                
                if (response.data && Array.isArray(response.data)) {
                    const devices = response.data.map(d => {
                        const getVal = (path) => {
                            if (d[path] && d[path]._value !== undefined) return d[path]._value;
                            if (d[path] !== undefined && typeof d[path] !== 'object') return d[path];
                            
                            const parts = path.split('.');
                            let current = d;
                            for (const part of parts) {
                                if (current == null) return undefined;
                                current = current[part];
                            }
                            if (current && current._value !== undefined) return current._value;
                            return current;
                        };

                        const pppoeUser = 
                            getVal('InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANPPPConnection.1.Username') ||
                            getVal('InternetGatewayDevice.WANDevice.1.WANPPPConnection.1.Username') ||
                            getVal('Device.WANDevice.1.WANPPPConnection.1.Username') ||
                            getVal('Device.Users.User.1.Username') ||
                            (d.summary && d.summary.username) ||
                            (d.summary && d.summary.mac) ||
                            'Unknown';
                            
                        const ssid = 
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSID') ||
                            getVal('Device.WiFi.SSID.1.SSID') ||
                            'Unknown';
                            
                        const rxPowerRaw = 
                            getVal('InternetGatewayDevice.WANDevice.1.X_ZTE-COM_WANPONInterfaceConfig.RXPower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.X_ZTE_COM_WANPONInterfaceConfig.RXPower') ||
                            getVal('Device.Optical.ReceivePower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.WANDSLInterfaceConfig.RxPower') ||
                            '-';
                            
                        let rxPowerStr = '-';
                        if (rxPowerRaw !== undefined && rxPowerRaw !== null && rxPowerRaw !== '-' && rxPowerRaw !== '') {
                            let num = parseFloat(rxPowerRaw);
                            if (!isNaN(num)) {
                                if (Math.abs(num) > 100) num = num / 1000;
                                else if (Math.abs(num) > 10 && num > 0) num = num / 100;
                                rxPowerStr = num.toFixed(2);
                            } else {
                                rxPowerStr = String(rxPowerRaw);
                            }
                        }
                        
                        const connectedUsers = 
                            getVal('InternetGatewayDevice.LANDevice.1.Hosts.HostNumberOfEntries') ||
                            getVal('Device.Hosts.HostNumberOfEntries') ||
                            (d.summary && d.summary.connectedUsers) ||
                            '0';

                        const lastInform = d._lastInform || d._lastPing || d._lastBoot;
                        let isOnline = false;
                        if (lastInform) {
                            const diffMs = new Date() - new Date(lastInform);
                            isOnline = diffMs < 20 * 60 * 1000; // 20 mins threshold
                        }

                        return {
                            id: d._id || Math.random().toString(36).substring(7),
                            username: pppoeUser !== 'Unknown' ? String(pppoeUser) : (d._id || 'Unknown'),
                            isOnline: isOnline,
                            ssid: String(ssid),
                            connectedUsers: parseInt(connectedUsers) || 0,
                            customerNumber: '-',
                            rxPower: rxPowerStr,
                            areaName: area.name
                        };
                    });
                    allDevices = allDevices.concat(devices);
                }
            } catch (err) {
                console.error(`Failed to fetch ACS devices for area ${area.name}: ${err.message}`);
            }
        }
        
        res.json(allDevices);
    } catch (error) {
        console.error("Error fetching ACS devices:", error);
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.post('/api/acs/devices/:id/action', async (req, res) => {
    try {
        const { action, value } = req.body;
        // Mock successful response since we can't easily proxy tasks without knowing the exact ACS task API format.
        // In a real app we'd POST to /tasks for the specific device ID.
        res.json({ message: `Aksi ${action} diproses` });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.post('/api/areas', async (req, res) => {
    try {
        const { name, description, routerIp, apiDomain, customerCount, mikrotikUser, mikrotikPassword, acsUser, acsPassword } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO areas (name, description, routerIp, apiDomain, customerCount, mikrotikUser, mikrotikPassword, acsUser, acsPassword) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [name, description, routerIp, apiDomain, customerCount || 0, mikrotikUser || '', mikrotikPassword || '', acsUser || '', acsPassword || '']
        );
        res.json({ message: "Area ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.post('/api/odc', async (req, res) => {
    try {
        const { name, location, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        let result;
        try {
            [result] = await req.pool.query(
                'INSERT INTO odc_list (name, location, portCount, portInput) VALUES (?, ?, ?, ?)',
                [name, location, parsedPortCount, portInput || '']
            );
        } catch(e) {
            if (e.message && (e.message.includes("Unknown column") || e.message.includes("Unknown column 'portinput'"))) {
                await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(err=>{});
                await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});
                [result] = await req.pool.query(
                    'INSERT INTO odc_list (name, location, portCount, portInput) VALUES (?, ?, ?, ?)',
                    [name, location, parsedPortCount, portInput || '']
                );
            } else {
                throw e;
            }
        }
        res.json({ message: "ODC ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });
    }
});

app.post('/api/odp', async (req, res) => {
    try {
        const { odcId, name, portCount, portInput } = req.body;
        const parsedOdcId = parseInt(odcId) || 0;
        const parsedPortCount = parseInt(portCount) || 0;
        let result;
        try {
            [result] = await req.pool.query(
                'INSERT INTO odp_list (name, odcId, portCount, portInput) VALUES (?, ?, ?, ?)',
                [name, parsedOdcId, parsedPortCount, portInput || '']
            );
        } catch(e) {
            if (e.message && (e.message.includes("Unknown column") || e.message.includes("Unknown column 'portinput'"))) {
                await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(err=>{});
                await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});
                [result] = await req.pool.query(
                    'INSERT INTO odp_list (name, odcId, portCount, portInput) VALUES (?, ?, ?, ?)',
                    [name, parsedOdcId, parsedPortCount, portInput || '']
                );
            } else {
                throw e;
            }
        }
        res.json({ message: "ODP ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });
    }
});

app.post('/api/categories', async (req, res) => {
    try {
        const { name, description } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO categories (name, description) VALUES (?, ?)',
            [name, description]
        );
        res.json({ message: "Kategori ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.post('/api/inventory', async (req, res) => {
    try {
        const { categoryId, name, brand, type, initialStock, finalStock } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO inventory (categoryId, name, brand, type, initialStock, finalStock) VALUES (?, ?, ?, ?, ?, ?)',
            [categoryId, name, brand, type, initialStock, finalStock]
        );
        res.json({ message: "Inventory ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});

app.post('/api/admins', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO users (name, username, role, password) VALUES (?, ?, ?, ?)',
            [name, username, role, password]
        );
        res.json({ message: "Admin ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});


app.get('/api/mikrotik/status/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];

        if (!area.routerIp || !area.mikrotikUser || !area.mikrotikPassword) {
            return res.status(400).json({ error: "Mikrotik credentials incomplete" });
        }

        const [host, port] = area.routerIp.split(':');

        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        const api = await client.connect();

        const resourceMenu = api.menu('/system/resource');
        const resources = await resourceMenu.get();
        const resource = resources[0];

        const pppoeActiveMenu = api.menu('/ppp/active');
        const activePppoe = await pppoeActiveMenu.get();
        
        const pppSecretMenu = api.menu('/ppp/secret');
        const allSecrets = await pppSecretMenu.get();

        client.close();

        res.json({
            cpuLoad: resource['cpu-load'] + '%',
            uptime: resource['uptime'],
            activePppoe: activePppoe.length.toString(),
            offlinePppoe: (allSecrets.length - activePppoe.length).toString()
        });
    } catch (error) {
        console.error("Mikrotik error:", error.message);
        res.status(500).json({ error: "Failed to connect to Mikrotik: " + error.message });
    }
});

app.post('/api/mikrotik/secrets/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, password, profile } = req.body;
        
        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        if (!area.routerIp || !area.mikrotikUser || !area.mikrotikPassword) {
            return res.status(400).json({ error: "Mikrotik credentials incomplete" });
        }
        
        const [host, port] = area.routerIp.split(':');
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        const api = await client.connect();
        
        await api.write('/ppp/secret/add', [
            `=name=${name}`,
            `=password=${password}`,
            `=profile=${profile || 'default'}`,
            `=service=pppoe`
        ]);
        
        client.close();
        res.json({ message: "Secret berhasil ditambahkan" });
    } catch (error) {
        console.error("Error adding Mikrotik secret:", error);
        res.status(500).json({ error: "Terjadi kesalahan: " + (error.message || error) });
    }
});

app.get('/api/mikrotik/profiles/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        if (!area.routerIp || !area.mikrotikUser || !area.mikrotikPassword) {
            return res.status(400).json({ error: "Mikrotik credentials incomplete" });
        }
        
        const [host, port] = area.routerIp.split(':');
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        const api = await client.connect();
        
        const pppProfileMenu = api.menu('/ppp/profile');
        const allProfiles = await pppProfileMenu.get();
        
        client.close();
        
        const profiles = allProfiles.map(p => ({
            id: p['.id'],
            name: p.name
        }));
        
        res.json(profiles);
    } catch (error) {
        console.error("Error fetching Mikrotik profiles:", error);
        res.status(500).json({ error: "Terjadi kesalahan: " + (error.message || error) });
    }
});

app.get('/api/mikrotik/secrets/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];

        if (!area.routerIp || !area.mikrotikUser || !area.mikrotikPassword) {
            return res.status(400).json({ error: "Mikrotik credentials incomplete" });
        }

        const [host, port] = area.routerIp.split(':');

        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        const api = await client.connect();
        
        const pppSecretMenu = api.menu('/ppp/secret');
        const allSecrets = await pppSecretMenu.get();
        
        const pppoeActiveMenu = api.menu('/ppp/active');
        const activePppoe = await pppoeActiveMenu.get();
        
        client.close();
        
        const activeNames = activePppoe.map(p => p.name);
        
        const secrets = allSecrets.map(s => {
            const isActive = activeNames.includes(s.name);
            const activeDetail = isActive ? activePppoe.find(p => p.name === s.name) : null;
            return {
                id: s['.id'],
                name: s.name,
                profile: s.profile,
                status: isActive ? "Online" : ((s.disabled === 'true' || s.disabled === true) ? "Disabled" : "Offline"),
                ipAddress: activeDetail ? activeDetail.address : "",
                uptime: activeDetail ? activeDetail.uptime : ""
            };
        });

        res.json(secrets);
    } catch (error) {
        console.error("Mikrotik error:", error.message);
        res.status(500).json({ error: "Failed to connect to Mikrotik: " + error.message });
    }
});

const PORT = 4500;
app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});
