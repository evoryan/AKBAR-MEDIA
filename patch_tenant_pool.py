import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

old_func = """function getTenantPool(dbName) {
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
}"""

new_func = """function getTenantPool(dbName) {
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
        tenantPools[dbName].query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0, ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e => { /* Ignore */ });
        tenantPools[dbName].query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0, ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e => { /* Ignore */ });
    }
    return tenantPools[dbName];
}"""

content = content.replace(old_func, new_func)

with open('VPS/server.js', 'w') as f:
    f.write(content)
