const mysql = require('mysql2/promise');
require('dotenv').config({ path: 'VPS/.env' });
async function test() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'app_db'
    });
    const [rows] = await pool.query('SELECT price, status FROM customers');
    console.log(rows);
    process.exit(0);
}
test();
