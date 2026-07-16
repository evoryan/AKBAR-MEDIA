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
        await pool.query("ALTER TABLE pembukuan ADD COLUMN category VARCHAR(50) DEFAULT 'Lain-lain'");
        console.log("Column added");
    } catch(e) {
        console.log(e.message);
    }
    process.exit(0);
}
alter();
