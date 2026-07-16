const mysql = require('mysql2/promise');
require('dotenv').config();

async function addPemasukanTable() {
    const masterPool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'akbar',
        password: process.env.DB_PASSWORD || '08Delapan',
        database: 'akbar_media_master',
    });

    try {
        console.log("Adding pemasukan table to master db...");
        await masterPool.query(`CREATE TABLE IF NOT EXISTS pemasukan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            category VARCHAR(100) UNIQUE,
            amount DOUBLE,
            description TEXT,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )`);
        console.log("Success in master db");

        const [users] = await masterPool.query('SELECT DISTINCT db_name FROM users WHERE db_name IS NOT NULL AND db_name != ""');
        for (const user of users) {
            const dbName = user.db_name;
            console.log(`Adding pemasukan table to ${dbName}...`);
            const pool = mysql.createPool({
                host: process.env.DB_HOST || 'localhost',
                user: process.env.DB_USER || 'akbar',
                password: process.env.DB_PASSWORD || '08Delapan',
                database: dbName,
            });
            try {
                await pool.query(`CREATE TABLE IF NOT EXISTS pemasukan (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    category VARCHAR(100) UNIQUE,
                    amount DOUBLE,
                    description TEXT,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )`);
                console.log(`Success in ${dbName}`);
            } catch (e) {
                console.error(`Failed in ${dbName}:`, e.message);
            }
            await pool.end();
        }
    } catch (e) {
        console.error("Error:", e.message);
    } finally {
        await masterPool.end();
    }
}

addPemasukanTable();
