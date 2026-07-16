const express = require('express');
const cron = require('node-cron');
const { RouterOSClient } = require('routeros-client');
const cors = require('cors');
const axios = require('axios');
const mysql = require('mysql2/promise');
const jwt = require('jsonwebtoken');
const { sendTenantNotification } = require('./fcm_service');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());
app.use((req, res, next) => { console.log(req.method, req.url); next(); });
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
        
        await masterPool.query(`CREATE TABLE IF NOT EXISTS pembukuan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            type VARCHAR(50),
            amount DOUBLE,
            description TEXT,
            category VARCHAR(100) DEFAULT 'Lain-lain',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )`).catch(e=>{});
        await masterPool.query(`CREATE TABLE IF NOT EXISTS pemasukan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            category VARCHAR(100) UNIQUE,
            amount DOUBLE,
            description TEXT,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )`).catch(e=>{});
        await masterPool.query(`CREATE TABLE IF NOT EXISTS pengeluaran (
            id INT AUTO_INCREMENT PRIMARY KEY,
            category VARCHAR(100) UNIQUE,
            amount DOUBLE,
            description TEXT,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )`).catch(e=>{});

        await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{}); await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{}); await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        
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
            
            try {
                await pool.query(`CREATE TABLE IF NOT EXISTS pemasukan (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    category VARCHAR(100) UNIQUE,
                    amount DOUBLE,
                    description TEXT,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )`);
                results.push(`${name}: pemasukan table added`);
            } catch(e) { results.push(`${name}: pemasukan table err: ${e.message}`); }
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
            
            await tPool.query(`CREATE TABLE IF NOT EXISTS pembukuan (
                id INT AUTO_INCREMENT PRIMARY KEY,
                type VARCHAR(50),
                amount DOUBLE,
                description TEXT,
                category VARCHAR(100) DEFAULT 'Lain-lain',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )`).catch(e=>{});
            await tPool.query(`CREATE TABLE IF NOT EXISTS pengeluaran (
                id INT AUTO_INCREMENT PRIMARY KEY,
                category VARCHAR(100) UNIQUE,
                amount DOUBLE,
                description TEXT,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )`).catch(e=>{});

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
        if (customers.length === 0) return res.status(400).json({ error: "Customer tidak ditemukan di database" });
        const customerName = customers[0].name;
        
        // Ensure table exists
        await req.pool.query(`ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS category VARCHAR(100)`).catch(e=>{});
        await req.pool.query(`ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)`).catch(e=>{});
        
        await req.pool.query(`ALTER TABLE tagihan_bulanan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)`).catch(e=>{});
        await req.pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            customer_id INT,
            bulan VARCHAR(50),
            tahun INT,
            amount DECIMAL(15, 2),
            status VARCHAR(50) DEFAULT 'BELUM BAYAR',
            admin_name VARCHAR(100),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
        )`).catch(e=>{});

        // Mark tagihan as LUNAS CASH
        const [tagihan] = await req.pool.query('SELECT id, bulan, tahun FROM tagihan_bulanan WHERE customer_id = ? AND status = "BELUM BAYAR" ORDER BY id ASC LIMIT 1', [customerId]);
        let desc = `Pembayaran tagihan pelanggan ${customerName}`;
        if (tagihan.length > 0) {
            await req.pool.query('ALTER TABLE tagihan_bulanan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)').catch(e=>{});
            await req.pool.query('UPDATE tagihan_bulanan SET status = "LUNAS CASH", admin_name = ? WHERE id = ?', [adminName, tagihan[0].id]);
            desc = `Pembayaran tagihan pelanggan ${customerName} (${tagihan[0].bulan} ${tagihan[0].tahun})`;
        }

        // Update customer status to LUNAS CASH
        await req.pool.query('UPDATE customers SET status = "LUNAS CASH" WHERE id = ?', [customerId]);

        // Add to pembukuan
        try {
            await req.pool.query('ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)').catch(e=>{});
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, category, admin_name) VALUES (?, ?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc, 'Transaksi Cash', adminName || 'Admin']);
        } catch (e) {
            console.error("Warning: category column might be missing in pembukuan", e.message);
            await req.pool.query('INSERT INTO pembukuan (type, amount, description, admin_name) VALUES (?, ?, ?, ?)', 
                ['pemasukan', totalAmount || 0, desc, adminName || 'Admin']);
        }
        
        // Add to pemasukan summary table
        try {
            await req.pool.query(
                'INSERT INTO pemasukan (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)',
                ['Transaksi Cash', totalAmount || 0, desc]
            );
        } catch (e) {
            console.error("Warning: failed to insert to pemasukan table", e.message);
        }

        // Add notification
        const notifMsg = `Pembayaran "${customerName}" di terima oleh "${adminName}"`;
        try {
            await req.pool.query('INSERT INTO notifications (message) VALUES (?)', [notifMsg]);
            
            if (req.user && req.user.db_name) {
                sendTenantNotification(req.user.db_name, "Pembayaran Diterima", notifMsg);
            }
            sendTenantNotification("superadmin", "Pembayaran Diterima", notifMsg);
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
        // Get customer name
        const [customers] = await req.pool.query('SELECT name FROM customers WHERE id = ?', [customerId]);
        if (customers.length === 0) return res.status(400).json({ error: "Customer tidak ditemukan di database" });
        const customerName = customers[0].name;
        
        // Ensure table exists
        await req.pool.query(`ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS category VARCHAR(100)`).catch(e=>{});
        await req.pool.query(`ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)`).catch(e=>{});
        
        await req.pool.query(`ALTER TABLE tagihan_bulanan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)`).catch(e=>{});
        await req.pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            customer_id INT,
            bulan VARCHAR(50),
            tahun INT,
            amount DECIMAL(15, 2),
            status VARCHAR(50) DEFAULT 'BELUM BAYAR',
            admin_name VARCHAR(100),
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
        
        // Also add to pengeluaran summary table
        try {
            await req.pool.query(
                'INSERT INTO pengeluaran (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)',
                ['Pengembalian Dana', refundAmount, desc]
            );
        } catch (e) {
            console.error("Warning: failed to insert to pengeluaran table", e.message);
        }

        res.json({ message: "Pembatalan berhasil dan dana dicatat di pembukuan" });
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
        
        let totalPemasukan = 0;
        let totalPengeluaran = 0;
        try {
            const [pemasukanRows] = await req.pool.query('SELECT SUM(amount) as total FROM pemasukan');
            totalPemasukan = pemasukanRows[0]?.total || 0;
        } catch (e) {
            // fallback if table doesn't exist yet
            const [pembukuan] = await req.pool.query('SELECT type, SUM(amount) as total FROM pembukuan GROUP BY type');
            pembukuan.forEach(row => {
                if (row.type === 'pemasukan') totalPemasukan += Number(row.total);
            });
        }
        
        try {
            const [pengeluaranRows] = await req.pool.query('SELECT SUM(amount) as total FROM pengeluaran');
            totalPengeluaran = pengeluaranRows[0]?.total || 0;
        } catch (e) {
            const [pembukuan] = await req.pool.query('SELECT type, SUM(amount) as total FROM pembukuan GROUP BY type');
            pembukuan.forEach(row => {
                if (row.type === 'pengeluaran') totalPengeluaran += Number(row.total);
            });
        }
        
        const monthlyRevenue = totalPemasukan - totalPengeluaran;

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
        
        // Auto add status column if not exist
        await masterPool.query("ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'").catch(e=>{});

        // Always check master database for login
        const [rows] = await masterPool.query('SELECT * FROM users WHERE username = ? AND password = ?', [username, password]);
        
        if (rows.length > 0) {
            const user = rows[0];
            
            if (user.status === 'DISABLED') {
                return res.status(403).json({ error: "Akun/Tenant Anda dinonaktifkan. Hubungi Administrator." });
            }

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



app.get('/api/uang-di-admin', async (req, res) => {
    try {
        const [pemasukan] = await req.pool.query(`
            SELECT admin_name as adminName, SUM(amount) as totalAmount, COUNT(*) as jmlPlggn 
            FROM pembukuan 
            WHERE type = 'pemasukan' AND admin_name IS NOT NULL
            GROUP BY admin_name
        `);
        const [setoran] = await req.pool.query(`
            SELECT admin_name as adminName, SUM(amount) as totalAmount
            FROM pembukuan 
            WHERE type = 'setor' AND admin_name IS NOT NULL
            GROUP BY admin_name
        `);
        const [pengeluaran] = await req.pool.query(`
            SELECT admin_name as adminName, SUM(amount) as totalAmount
            FROM pembukuan 
            WHERE type = 'pengeluaran' AND admin_name IS NOT NULL
            GROUP BY admin_name
        `);
        
        let result = {};
        pemasukan.forEach(row => {
            result[row.adminName] = { 
                adminName: row.adminName, 
                totalDiterima: Number(row.totalAmount), 
                jmlPlggn: row.jmlPlggn,
                setor: 0,
                pengeluaran: 0
            };
        });
        setoran.forEach(row => {
            if (!result[row.adminName]) result[row.adminName] = { adminName: row.adminName, totalDiterima: 0, jmlPlggn: 0, setor: 0, pengeluaran: 0 };
            result[row.adminName].setor = Number(row.totalAmount);
        });
        pengeluaran.forEach(row => {
            if (!result[row.adminName]) result[row.adminName] = { adminName: row.adminName, totalDiterima: 0, jmlPlggn: 0, setor: 0, pengeluaran: 0 };
            result[row.adminName].pengeluaran = Number(row.totalAmount);
        });
        
        res.json(Object.values(result));
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
        
        // Fetch from pengeluaran table removed to prevent double-counting
        // Since all pengeluaran items are now stored in pembukuan table

        res.json(summary);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});



app.get('/api/pembayaran', async (req, res) => {
    try {
        await req.pool.query('ALTER TABLE tagihan_bulanan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)').catch(e=>{});
        const [rows] = await req.pool.query(`
            SELECT 
                t.id, t.bulan, t.tahun, t.amount, t.admin_name, t.created_at,
                c.name as customer_name, c.phone, c.address as area
            FROM tagihan_bulanan t
            JOIN customers c ON t.customer_id = c.id
            WHERE t.status = 'LUNAS CASH'
            ORDER BY t.created_at DESC
        `);
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/pembukuan/test_all', async (req, res) => {
    try {
        const [rows] = await masterPool.query('SELECT * FROM pembukuan ORDER BY id DESC');
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/pembukuan/all', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT * FROM pembukuan ORDER BY id DESC');
        res.json(rows.map(r => ({ ...r, id: r.id.toString(), amount: Number(r.amount) })));
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.put('/api/pembukuan/:id', async (req, res) => {
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
});

app.delete('/api/pembukuan/:id', async (req, res) => {
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
});

app.post('/api/setoran', async (req, res) => {
    try {
        const { adminName, amount } = req.body;
        await req.pool.query('ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)').catch(e=>{});
        await req.pool.query(
            'INSERT INTO pembukuan (type, amount, description, category, admin_name) VALUES (?, ?, ?, ?, ?)',
            ['setor', amount, `Setoran oleh ${adminName}`, 'Setoran', adminName]
        );
        res.json({ message: "Setoran berhasil ditambahkan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});


app.post('/api/pembukuan', async (req, res) => {
    try {
        await req.pool.query("ALTER TABLE pembukuan MODIFY COLUMN type VARCHAR(50)").catch(e => {});
        const { type, amount, description, category } = req.body;
        const cat = category || 'Lain-lain';
        try {
            await req.pool.query(
                'INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)',
                [type, amount || 0, description || '', cat]
            );
        } catch (e) {
            await req.pool.query(
                'INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)',
                [type, amount || 0, description || '']
            );
        }
        
        // Update summary table
        if (type === 'pemasukan') {
            await req.pool.query(
                'INSERT INTO pemasukan (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)',
                [cat, amount || 0, description || '']
            ).catch(e => console.error(e));
        } else if (type === 'pengeluaran') {
            await req.pool.query(
                'INSERT INTO pengeluaran (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), description = VALUES(description)',
                [cat, amount || 0, description || '']
            ).catch(e => console.error(e));
        }

        res.json({ message: "Pembukuan ditambahkan" });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});


app.get('/api/pemasukan', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT category, amount, description, updated_at FROM pemasukan');
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.post('/api/pemasukan', async (req, res) => {
    try {
        const { category, amount, description } = req.body;
        await req.pool.query(
            'INSERT INTO pemasukan (category, amount, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = VALUES(amount), description = VALUES(description)',
            [category, amount || 0, description || '']
        );
        res.json({ message: "Pemasukan diupdate" });
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
        const db_name = req.user ? req.user.db_name : 'app_db';
        const [rows] = await masterPool.query('SELECT id, name, username, role FROM users WHERE db_name = ?', [db_name]);
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
        const db_name = req.user ? req.user.db_name : 'app_db';
        await masterPool.query('DELETE FROM users WHERE id = ? AND db_name = ?', [req.params.id, db_name]);
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
        if (customers.length === 0) return res.status(400).json({ error: "Customer tidak ditemukan di database" });
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

app.post('/api/customers/:id/isolir', tenantContext, async (req, res) => {
    try {
        const { id } = req.params;
        console.log("ISOLIR REQUEST for id:", id);
        const [customers] = await req.pool.query('SELECT * FROM customers WHERE id = ?', [id]);
        if (customers.length === 0) {
            console.log("Customer not found in DB:", id);
            return res.status(400).json({ error: "Customer tidak ditemukan di database" });
        }
        const customer = customers[0];

        const identifier = customer.pppoe_secret || customer.username;
        if (!identifier) return res.status(400).json({ error: "Customer does not have PPPoE secret or username" });

        let [areas] = await req.pool.query('SELECT * FROM areas WHERE name = ?', [customer.area]);
        if (areas.length === 0) {
            // Fallback to first available area if not found, useful if area="Semua"
            const [allAreas] = await req.pool.query('SELECT * FROM areas LIMIT 1');
            if (allAreas.length > 0) {
                areas = allAreas;
            } else {
                return res.status(400).json({ error: "Area router tidak ditemukan" });
            }
        }
        const area = areas[0];

        let host = area.routerIp;
        let port = 8728;
        if (host.includes(':')) {
            const parts = host.split(':');
            host = parts[0];
            port = parseInt(parts[1]);
        }
        
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        await client.connect();
        
        // Find exact item safely
        let realId = null;
        let secretName = null;
        let results = await client.rosApi.write('/ppp/secret/print', [`?.id=${identifier}`]);
        console.log("Mikrotik print result for .id=", identifier, " is ", results);
        if (results.length > 0) {
            realId = results[0]['.id'] || results[0].id;
            secretName = results[0].name;
        } else {
            results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
            console.log("Mikrotik print result for name=", identifier, " is ", results);
            if (results.length > 0) {
                realId = results[0]['.id'] || results[0].id;
                secretName = results[0].name;
            }
        }

        if (realId) {
            await client.rosApi.write('/ppp/secret/set', [
                `=.id=${realId}`,
                `=disabled=yes`
            ]);
            
            if (secretName) {
                try {
                    let activeResults = await client.rosApi.write('/ppp/active/print', [`?name=${secretName}`]);
                    if (activeResults.length > 0) {
                        let activeId = activeResults[0]['.id'] || activeResults[0].id;
                        await client.rosApi.write('/ppp/active/remove', [
                            `=.id=${activeId}`
                        ]);
                    }
                } catch (e) {
                    console.error("Failed to remove active connection:", e.message);
                }
            }
        } else {
            console.log("Secret not found in Mikrotik, but proceeding as success to avoid breaking flow");
        }
        
        client.close();
        
        // Update customer status in database
        try {
            await req.pool.query('UPDATE customers SET status = ? WHERE id = ?', ['ISOLIR', id]);
        } catch (dbErr) {
            console.error("Error updating customer status:", dbErr);
        }

        res.json({ message: "Pelanggan berhasil di-isolir" });
    } catch (error) {
        console.error("Error isolating customer:", error);
        let msg = error.message;
        if (error.stack && error.stack.includes('routeros')) {
             msg = "Koneksi ke Mikrotik gagal atau timeout.";
        }
        res.status(500).json({ error: "Gagal mengisolir pelanggan: " + msg });
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

                        const pppoeUser = (() => {
                            for (let dev = 1; dev <= 2; dev++) {
                                for (let i = 1; i <= 10; i++) {
                                    for (let j = 1; j <= 5; j++) {
                                        let u = getVal(`InternetGatewayDevice.WANDevice.${dev}.WANConnectionDevice.${i}.WANPPPConnection.${j}.Username`) ||
                                                getVal(`Device.WANDevice.${dev}.WANConnectionDevice.${i}.WANPPPConnection.${j}.Username`);
                                        if (u && String(u).trim() !== '') return u;
                                    }
                                }
                            }
                            return getVal('InternetGatewayDevice.WANDevice.1.WANPPPConnection.1.Username') ||
                                   getVal('Device.WANDevice.1.WANPPPConnection.1.Username') ||
                                   getVal('Device.PPP.Interface.1.Username') ||
                                   getVal('Device.Users.User.1.Username') ||
                                   (d.summary && d.summary.username) ||
                                   (d.summary && d.summary.mac) ||
                                   'Unknown';
                        })();
                            
                        const ssid = 
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSID') ||
                            getVal('Device.WiFi.SSID.1.SSID') ||
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSIDAdvertisementEnabled') ||
                            'Unknown';
                            
                        const wifiPassword = 
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.KeyPassphrase') ||
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.PreSharedKey') ||
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.KeyPassphrase') ||
                            getVal('Device.WiFi.Radio.1.Security.KeyPassphrase') ||
                            '-';
                            
                        let rxPowerStr = '-';
                        const getAny = (pathTpl) => {
                            if (pathTpl.includes('*')) {
                                for (let i = 1; i <= 5; i++) {
                                    let v = getVal(pathTpl.replace('*', i));
                                    if (v !== undefined && v !== null && v !== '') return v;
                                }
                            } else {
                                let v = getVal(pathTpl);
                                if (v !== undefined && v !== null && v !== '') return v;
                            }
                            return undefined;
                        };

                        let zte = getAny("InternetGatewayDevice.WANDevice.*.X_ZTE-COM_WANPONInterfaceConfig.RXPower") || getAny("InternetGatewayDevice.WANDevice.*.X_ZTE_COM_WANPONInterfaceConfig.RXPower");
                        let huawei = getAny("InternetGatewayDevice.WANDevice.*.X_GponInterafceConfig.RXPower") || getAny("InternetGatewayDevice.WANDevice.*.X_HW_Optical.RxPower") || getAny("InternetGatewayDevice.WANDevice.*.X_HW_Optical.RxOpticalPower");
                        let fiberhome = getAny("InternetGatewayDevice.WANDevice.*.X_FH_GponInterfaceConfig.RXPower");
                        let ztecmcc = getAny("InternetGatewayDevice.WANDevice.*.X_CMCC_EponInterfaceConfig.RXPower");
                        let ztecmcg = getAny("InternetGatewayDevice.WANDevice.*.X_CMCC_GponInterfaceConfig.RXPower");
                        let gm220se = getAny("InternetGatewayDevice.WANDevice.*.X_CT-COM_EponInterfaceConfig.RXPower");
                        let gm220sg = getAny("InternetGatewayDevice.WANDevice.*.X_CT-COM_GponInterfaceConfig.RXPower") || getAny("InternetGatewayDevice.WANDevice.*.X_CT-COM_PONInterfaceConfig.RXPower") || getAny("InternetGatewayDevice.WANDevice.*.X_CT-COM_XPON.RXPower");
                        let f477v2 = getAny("InternetGatewayDevice.WANDevice.*.X_CU_WANEPONInterfaceConfig.OpticalTransceiver.RXPower");
                        let nokia = getAny("InternetGatewayDevice.X_ALU_OntOpticalParam.RXPower");
                        let generic = getAny("Device.Optical.ReceivePower") || getAny("InternetGatewayDevice.WANDevice.*.WANDSLInterfaceConfig.RxPower");
                        
                        const calcDb = (val, isLog) => {
                            let num = parseFloat(val);
                            if (isNaN(num)) return val;
                            if (num < 0) {
                                if (num < -1000) num = num / 1000;
                                else if (num < -100) num = num / 100;
                                return num.toFixed(2);
                            } else if (num > 0 && isLog) {
                                let db = 30 + (Math.log10(num * Math.pow(10, -7)) * 10);
                                return (Math.ceil(db * 100) / 100).toFixed(2);
                            } else if (num > 0 && !isLog) {
                                if (num > 1000) num = -(num / 100);
                                else if (num > 100) num = -(num / 10);
                                else num = -num;
                                return num.toFixed(2);
                            }
                            return num.toFixed(2);
                        };

                        let m = undefined;
                        if (zte !== undefined) m = calcDb(zte, true);
                        else if (ztecmcc !== undefined) m = calcDb(ztecmcc, true);
                        else if (ztecmcg !== undefined) m = calcDb(ztecmcg, true);
                        else if (gm220se !== undefined) m = calcDb(gm220se, true);
                        else if (gm220sg !== undefined) m = calcDb(gm220sg, true);
                        else if (f477v2 !== undefined) m = calcDb(f477v2, true);
                        else if (huawei !== undefined) m = calcDb(huawei, false);
                        else if (fiberhome !== undefined) m = calcDb(fiberhome, false);
                        else if (nokia !== undefined) m = calcDb(nokia, false);
                        else if (generic !== undefined) m = calcDb(generic, false);

                        if (m !== undefined && m !== "N/A" && m !== null) {
                            rxPowerStr = String(m);
                        }
                        
                        const connectedUsers = (() => {
                            let count = parseInt(getVal('InternetGatewayDevice.LANDevice.1.Hosts.HostNumberOfEntries') || getVal('Device.Hosts.HostNumberOfEntries') || (d.summary && d.summary.connectedUsers) || '0');
                            if (count === 0 || isNaN(count)) {
                                // Try AssociatedDevice
                                let assocCount = 0;
                                for (let i = 1; i <= 3; i++) {
                                    for (let j = 1; j <= 5; j++) {
                                        let assoc = getVal(`InternetGatewayDevice.LANDevice.${i}.WLANConfiguration.${j}.AssociatedDevice`);
                                        if (assoc && typeof assoc === 'object') {
                                            assocCount += Object.keys(assoc).filter(k => !k.startsWith('_')).length;
                                        }
                                    }
                                }
                                if (assocCount > 0) return assocCount;
                            }
                            return count;
                        })();

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
                            wifiPassword: String(wifiPassword),
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
        const { action, value, areaName } = req.body;
        const deviceId = req.params.id;
        
        let areaRow = null;
        if (areaName) {
            const [areas] = await req.pool.query('SELECT * FROM areas WHERE name = ?', [areaName]);
            if (areas.length > 0) areaRow = areas[0];
        }
        
        if (!areaRow) {
            // Fallback to searching all areas
            const [areas] = await req.pool.query('SELECT * FROM areas WHERE apiDomain IS NOT NULL AND apiDomain != ""');
            areaRow = areas[0]; // just try the first one for now if we can't find it
        }
        
        if (!areaRow || !areaRow.apiDomain) {
            return res.status(400).json({ error: "Server Area tidak ditemukan atau URL API kosong" });
        }
        
        let baseUrl = areaRow.apiDomain.trim();
        if (baseUrl.endsWith('/')) baseUrl = baseUrl.slice(0, -1);
        
        let taskData = {
            device: deviceId
        };
        
        if (action === 'set_ssid') {
            taskData.name = 'setParameterValues';
            taskData.parameterValues = [
                ['InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSID', String(value), 'xsd:string'],
                ['Device.WiFi.SSID.1.SSID', String(value), 'xsd:string']
            ];
        } else if (action === 'set_password') {
            taskData.name = 'setParameterValues';
            taskData.parameterValues = [
                ['InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.PreSharedKey', String(value), 'xsd:string'],
                ['InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.KeyPassphrase', String(value), 'xsd:string'],
                ['Device.WiFi.Radio.1.Security.KeyPassphrase', String(value), 'xsd:string']
            ];
        } else if (action === 'reboot') {
            taskData.name = 'reboot';
        } else {
            return res.status(400).json({ error: "Aksi tidak dikenal" });
        }
        
        // GenieACS v1.2 format: POST /tasks?connection_request
        const response = await axios.post(`${baseUrl}/tasks?connection_request`, taskData, {
            auth: { username: areaRow.acsUser, password: areaRow.acsPassword },
            timeout: 10000 // give it some time to process
        });
        
        res.json({ message: `Aksi ${action} berhasil diproses` });
    } catch (error) {
        console.error("ACS Task Error:", error.message); 
        let errorMsg = error.message;
        if (error.response && error.response.data) {
            errorMsg = typeof error.response.data === 'string' ? error.response.data : JSON.stringify(error.response.data);
        }
        res.status(500).json({ error: `Gagal ke ACS: ${errorMsg}` });
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


app.put('/api/admins/:id', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const db_name = req.user ? req.user.db_name : 'app_db';
        // Check if username is already taken by someone else
        const [existing] = await masterPool.query('SELECT id FROM users WHERE username = ? AND id != ?', [username, req.params.id]);
        if (existing.length > 0) {
            return res.status(400).json({ error: "Username sudah digunakan" });
        }
        
        if (password) {
            await masterPool.query('UPDATE users SET name = ?, username = ?, role = ?, password = ? WHERE id = ? AND db_name = ?', [name, username, role || 'ADMIN', password, req.params.id, db_name]);
        } else {
            await masterPool.query('UPDATE users SET name = ?, username = ?, role = ? WHERE id = ? AND db_name = ?', [name, username, role || 'ADMIN', req.params.id, db_name]);
        }
        res.json({ message: "Admin diperbarui" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});
app.post('/api/admins', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const db_name = req.user ? req.user.db_name : 'app_db';
        const [result] = await masterPool.query(
            'INSERT INTO users (name, username, role, password, db_name) VALUES (?, ?, ?, ?, ?)',
            [name, username, role || 'ADMIN', password || '', db_name]
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


app.post('/api/mikrotik/secrets/:id/disable', async (req, res) => {
    try {
        const { id } = req.params;
        const identifier = req.body.secretId || req.body.secretName;
        
        if (!identifier) return res.status(400).json({ error: "secretId is required" });

        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        let host = area.routerIp;
        let port = 8728;
        if (host.includes(':')) {
            const parts = host.split(':');
            host = parts[0];
            port = parseInt(parts[1]);
        }
        
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        await client.connect();
        
        // Find exact item safely
        let realId = null;
        let secretName = null;
        let results = await client.rosApi.write('/ppp/secret/print', [`?.id=${identifier}`]);
        console.log("Mikrotik print result for .id=", identifier, " is ", results);
        if (results.length > 0) {
            realId = results[0]['.id'] || results[0].id;
            secretName = results[0].name;
        } else {
            results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
            console.log("Mikrotik print result for name=", identifier, " is ", results);
            if (results.length > 0) {
                realId = results[0]['.id'] || results[0].id;
                secretName = results[0].name;
            }
        }

        if (!realId) {
            client.close();
            return res.status(400).json({ error: "Secret tidak ditemukan di Mikrotik" });
        }
        
        await client.rosApi.write('/ppp/secret/set', [
            `=.id=${realId}`,
            `=disabled=yes`
        ]);
        
        if (secretName) {
            try {
                let activeResults = await client.rosApi.write('/ppp/active/print', [`?name=${secretName}`]);
                if (activeResults.length > 0) {
                    let activeId = activeResults[0]['.id'] || activeResults[0].id;
                    await client.rosApi.write('/ppp/active/remove', [
                        `=.id=${activeId}`
                    ]);
                }
            } catch (e) {
                console.error("Failed to remove active connection:", e.message);
            }
        }
        
        client.close();
        res.json({ message: "Secret berhasil di-disable dan active connection diremove (jika ada)" });
    } catch (error) {
        console.error("Error disabling Mikrotik secret:", error);
        res.status(500).json({ error: "Gagal mendisable secret: " + error.message });
    }
});
app.post('/api/mikrotik/secrets/:id/enable', async (req, res) => {
    try {
        const { id } = req.params;
        const identifier = req.body.secretId || req.body.secretName;
        
        if (!identifier) return res.status(400).json({ error: "secretId is required" });

        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        let host = area.routerIp;
        let port = 8728;
        if (host.includes(':')) {
            const parts = host.split(':');
            host = parts[0];
            port = parseInt(parts[1]);
        }
        
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        await client.connect();
        
        let realId = null;
        let results = await client.rosApi.write('/ppp/secret/print', [`?.id=${identifier}`]);
        console.log("Mikrotik print result for .id=", identifier, " is ", results);
        if (results.length > 0) {
            realId = results[0]['.id'] || results[0].id;
        } else {
            results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
            if (results.length > 0) realId = results[0]['.id'] || results[0].id;
        }

        if (!realId) {
            client.close();
            return res.status(400).json({ error: "Secret tidak ditemukan di Mikrotik" });
        }
        
        await client.rosApi.write('/ppp/secret/set', [
            `=.id=${realId}`,
            `=disabled=no`
        ]);
        
        client.close();
        res.json({ message: "Secret berhasil di-enable" });
    } catch (error) {
        console.error("Error enabling Mikrotik secret:", error);
        res.status(500).json({ error: "Gagal meng-enable secret: " + error.message });
    }
});
app.post('/api/mikrotik/secrets/:id/remove-active', async (req, res) => {
    try {
        const { id } = req.params;
        const identifier = req.body.secretName || req.body.secretId;
        
        if (!identifier) return res.status(400).json({ error: "secretName is required" });

        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        let host = area.routerIp;
        let port = 8728;
        if (host.includes(':')) {
            const parts = host.split(':');
            host = parts[0];
            port = parseInt(parts[1]);
        }
        
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        await client.connect();
        
        let activeId = null;
        let results = await client.rosApi.write('/ppp/active/print', [`?name=${identifier}`]);
        if (results.length > 0) {
            activeId = results[0]['.id'] || results[0].id;
        } else {
            results = await client.rosApi.write('/ppp/active/print', [`?.id=${identifier}`]);
            if (results.length > 0) activeId = results[0]['.id'] || results[0].id;
        }

        if (activeId) {
            await client.rosApi.write('/ppp/active/remove', [
                `=.id=${activeId}`
            ]);
            client.close();
            return res.json({ message: "Active connection berhasil diremove" });
        } else {
            client.close();
            return res.status(404).json({ error: "Active connection tidak ditemukan" });
        }
    } catch (error) {
        console.error("Error removing active connection:", error);
        res.status(500).json({ error: "Gagal meremove active connection: " + error.message });
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
                id: s['.id'] || s.id,
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

const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});
server.keepAliveTimeout = 65000;
server.headersTimeout = 66000;
