const mysql = require('mysql2/promise');
require('dotenv').config();
const fs = require('fs');

async function addTenant() {
    const args = process.argv.slice(2);
    if (args.length < 4) {
        console.log("Penggunaan: node add_tenant.js <db_name> <username> <password> <name> [is_demo: 0 atau 1]");
        console.log("Contoh: node add_tenant.js klien_baru admin_klien secret123 'Admin Klien' 1");
        process.exit(1);
    }

    const [dbName, username, password, name, isDemoArg] = args;
    const isDemo = isDemoArg === '1' || isDemoArg === 'true' ? 1 : 0;

    try {
        // Koneksi tanpa spesifik DB untuk membuat DB baru
        const connection = await mysql.createConnection({
            host: process.env.DB_HOST || 'localhost',
            user: process.env.DB_USER || 'akbar',
            password: process.env.DB_PASSWORD || '08Delapan',
            multipleStatements: true
        });

        console.log(`[1/3] Membuat database tenant: ${dbName}...`);
        await connection.query(`CREATE DATABASE IF NOT EXISTS \`${dbName}\``);

        console.log(`[2/3] Menginisialisasi tabel-tabel pada database ${dbName}...`);
        let initSql = fs.readFileSync('init.sql', 'utf8');
        initSql = initSql.replace(/CREATE DATABASE IF NOT EXISTS app_db;/g, '');
        initSql = initSql.replace(/USE app_db;/g, '');
        
        await connection.query(`USE \`${dbName}\``);
        await connection.query(initSql);

        console.log(`[3/3] Menambahkan akun superadmin '${username}' ke master database...`);
        await connection.query(`USE akbar_media_master`);
        
        // Cek apakah username sudah ada
        const [existing] = await connection.query(`SELECT * FROM users WHERE username = ?`, [username]);
        if (existing.length > 0) {
            console.log(`Error: Username '${username}' sudah digunakan!`);
        } else {
            // Pastikan kolom is_demo tersedia
            await connection.query(`ALTER TABLE users ADD COLUMN is_demo TINYINT(1) DEFAULT 0`).catch(e=>{});
            
            await connection.query(`
                INSERT INTO users (name, username, password, role, db_name, is_demo)
                VALUES (?, ?, ?, 'SUPER_ADMIN', ?, ?)
            `, [name, username, password, dbName, isDemo]);
            console.log(`Berhasil! Tenant baru '${dbName}' dengan user '${username}' (Demo: ${isDemo ? 'Ya' : 'Tidak'}) berhasil ditambahkan.`);
        }

        await connection.end();
    } catch (err) {
        console.error("Terjadi kesalahan:", err.message);
    }
}

addTenant();
