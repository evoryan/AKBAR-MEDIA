with open('/tmp/server.js', 'r') as f:
    content = f.read()

# Replace pool with masterPool and tenant logic
new_content = """const express = require('express');
const { RouterOSClient } = require('routeros-client');
const cors = require('cors');
const axios = require('axios');
const mysql = require('mysql2/promise');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

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
    }
    return tenantPools[dbName];
}

// Middleware to extract tenant context
const tenantContext = (req, res, next) => {
    // Skip auth for login
    if (req.path === '/api/login') {
        return next();
    }
    
    // For now, allow bypassing for simple tests or if token is in header
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.split(' ')[1];
        try {
            const decoded = jwt.verify(token, JWT_SECRET);
            req.user = decoded;
            if (decoded.db_name) {
                req.pool = getTenantPool(decoded.db_name);
            } else {
                req.pool = masterPool; // fallback
            }
            return next();
        } catch (err) {
            console.error("JWT verify error:", err.message);
            // Fallback for current app without token
            req.pool = masterPool;
            return next();
        }
    } else {
        // Fallback for current app which doesn't send token yet
        // Ideally we should reject this in a strict environment
        req.pool = masterPool;
        return next();
    }
};

app.use(tenantContext);
"""

# Extract all app.get/post etc.
import re
routes_start = content.find("app.get('/api/dashboard/pppoe-offline'")
if routes_start == -1:
    print("Cannot find routes")
else:
    routes_part = content[routes_start:]
    # Replace `pool.query` with `req.pool.query`
    routes_part = re.sub(r'\bpool\.query\b', 'req.pool.query', routes_part)
    
    # We also need to fix /api/login specifically
    login_start = routes_part.find("app.post('/api/login'")
    
    # We will just replace the whole login route
    login_route = """app.post('/api/login', async (req, res) => {
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
        res.status(500).json({ error: "Terjadi kesalahan" });
    }
});"""
    
    # Replace the old login route
    routes_part = re.sub(r"app\.post\('/api/login', async \(req, res\) => \{.*?(?=app\.(get|post|put|delete)\()", login_route + "\n", routes_part, flags=re.DOTALL)
    
    final_content = new_content + routes_part
    with open('/tmp/server_new.js', 'w') as out:
        out.write(final_content)
        
