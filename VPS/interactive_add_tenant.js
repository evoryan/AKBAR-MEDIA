const mysql = require('mysql2/promise');
require('dotenv').config();
const fs = require('fs');
const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const askQuestion = (query) => new Promise((resolve) => rl.question(query, resolve));

async function addTenantInteractive() {
    console.log("=== Setup Tenant Baru ===");
    
    try {
        const dbName = await askQuestion("1. Masukkan nama database tenant (contoh: akbar_media_client2): ");
        if (!dbName) {
            console.log("Nama database tidak boleh kosong!");
            process.exit(1);
        }

        const name = await askQuestion("2. Masukkan Nama Lengkap Admin (contoh: Budi Susanto): ");
        if (!name) {
            console.log("Nama Admin tidak boleh kosong!");
            process.exit(1);
        }

        const username = await askQuestion("3. Masukkan Username untuk Login: ");
        if (!username) {
            console.log("Username tidak boleh kosong!");
            process.exit(1);
        }

        const password = await askQuestion("4. Masukkan Password untuk Login: ");
        if (!password) {
            console.log("Password tidak boleh kosong!");
            process.exit(1);
        }

        rl.close();

        console.log("\nMemulai proses setup...");

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
            console.log(`Error: Username '${username}' sudah digunakan! Silakan ulangi dengan username lain.`);
        } else {
            await connection.query(`
                INSERT INTO users (name, username, password, role, db_name)
                VALUES (?, ?, ?, 'SUPER_ADMIN', ?)
            `, [name, username, password, dbName]);
            console.log(`\n✅ Berhasil! Tenant baru '${dbName}' dengan user '${username}' berhasil ditambahkan.`);
            console.log("Anda sekarang bisa login di aplikasi menggunakan akun tersebut.");
        }

        await connection.end();
    } catch (err) {
        console.error("\n❌ Terjadi kesalahan:", err.message);
        rl.close();
    }
}

addTenantInteractive();
