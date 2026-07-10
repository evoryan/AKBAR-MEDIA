const mysql = require('mysql2/promise');
require('dotenv').config();
async function run() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'app_db',
    });
    try {
        await pool.query(`
            CREATE TABLE IF NOT EXISTS packages (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                speed VARCHAR(255) NOT NULL,
                price DECIMAL(10, 2) NOT NULL,
                taxRate DECIMAL(5, 2) DEFAULT 11.0,
                pppoeProfile VARCHAR(255) DEFAULT 'default',
                description TEXT
            )
        `);
        console.log("Table packages created");
    } catch (e) {
        console.error(e);
    }
    process.exit(0);
}
run();
