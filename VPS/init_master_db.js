const mysql = require('mysql2/promise');
require('dotenv').config();

async function initMasterDB() {
    try {
        const connection = await mysql.createConnection({
            host: process.env.DB_HOST || 'localhost',
            user: process.env.DB_USER || 'akbar',
            password: process.env.DB_PASSWORD || '08Delapan'
        });

        console.log("Connected to MySQL server.");

        await connection.query(`CREATE DATABASE IF NOT EXISTS akbar_media_master`);
        console.log("Database akbar_media_master created or already exists.");

        await connection.query(`USE akbar_media_master`);

        await connection.query(`
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                username VARCHAR(50) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
                db_name VARCHAR(100) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);
        console.log("Table 'users' created or already exists.");

        const [rows] = await connection.query(`SELECT * FROM users WHERE username = 'akbar2026'`);
        if (rows.length === 0) {
            await connection.query(`
                INSERT INTO users (name, username, password, role, db_name)
                VALUES ('Super Admin', 'akbar2026', '08Delapan', 'SUPER_ADMIN', 'app_db')
            `);
            console.log("Default superadmin 'akbar2026' inserted.");
        } else {
            console.log("Default superadmin 'akbar2026' already exists.");
        }

        await connection.end();
        console.log("Done.");
    } catch (err) {
        console.error("Error initializing master database:", err);
    }
}

initMasterDB();
