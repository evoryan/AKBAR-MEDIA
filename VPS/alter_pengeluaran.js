const mysql = require('mysql2/promise');
require('dotenv').config();

async function alter() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'akbar',
        password: process.env.DB_PASSWORD || '08Delapan',
        database: process.env.DB_NAME || 'app_db',
    });
    try {
        await pool.query(`
            CREATE TABLE IF NOT EXISTS pengeluaran (
                id INT AUTO_INCREMENT PRIMARY KEY,
                category VARCHAR(255) UNIQUE,
                amount DECIMAL(15,2) DEFAULT 0,
                description TEXT,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        `);
        console.log("Table pengeluaran created/exists");
    } catch(e) {
        console.log(e.message);
    }
    process.exit(0);
}
alter();
