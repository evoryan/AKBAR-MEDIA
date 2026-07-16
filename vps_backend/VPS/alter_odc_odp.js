const mysql = require('mysql2/promise');
require('dotenv').config({ path: 'VPS/.env' });

async function alter() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || 'password',
        database: process.env.DB_NAME || 'aistudio',
    });
    try {
        await pool.query("ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0");
        await pool.query("ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''");
        await pool.query("ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''");
        console.log("Columns added successfully");
    } catch(e) {
        console.log(e.message);
    }
    process.exit(0);
}
alter();
