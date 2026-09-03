const mysql = require('mysql2/promise');
require('dotenv').config();
const fs = require('fs');
const path = require('path');
const readline = require('readline');

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const askQuestion = (query) => new Promise((resolve) => rl.question(query, resolve));

async function executeSqlFile(conn, filePath, targetDb) {
    if (!fs.existsSync(filePath)) {
        console.log(`⚠️ Peringatan: File '${filePath}' tidak ditemukan.`);
        return;
    }
    await conn.query(`USE \`${targetDb}\``);
    const rawSql = fs.readFileSync(filePath, 'utf8');

    const noComments = rawSql
        .replace(/\/\*[\s\S]*?\*\//g, '')
        .replace(/^[ \t]*--.*$/gm, '')
        .replace(/^[ \t]*#.*$/gm, '');

    const statements = noComments
        .split(';')
        .map(s => s.trim())
        .filter(s => s.length > 0);

    for (const stmt of statements) {
        if (/^CREATE\s+DATABASE/i.test(stmt) || /^USE\s+/i.test(stmt)) {
            continue;
        }
        try {
            await conn.query(stmt);
        } catch (err) {
            if (!err.message.includes('already exists')) {
                console.log(`   ⚠️ Notice SQL: ${err.message}`);
            }
        }
    }
}

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

        const isDemoResponse = await askQuestion("5. Apakah ini tenant DEMO? (y/N): ");
        const isDemo = isDemoResponse.toLowerCase() === 'y' ? 1 : 0;

        rl.close();

        console.log("\nMemulai proses setup...");

        const connection = await mysql.createConnection({
            host: process.env.DB_HOST || 'localhost',
            user: process.env.DB_USER || 'akbar',
            password: process.env.DB_PASSWORD || '08Delapan',
            multipleStatements: true
        });

        console.log(`[1/3] Membuat database tenant: ${dbName}...`);
        await connection.query(`CREATE DATABASE IF NOT EXISTS \`${dbName}\``);

        console.log(`[2/3] Menginisialisasi tabel-tabel pada database ${dbName}...`);
        const initSqlPath = path.join(__dirname, 'init.sql');
        await executeSqlFile(connection, initSqlPath, dbName);

        console.log(`[3/3] Menambahkan akun superadmin '${username}' ke master database...`);
        await connection.query(`USE akbar_media_master`);
        
        // Cek apakah username sudah ada
        const [existing] = await connection.query(`SELECT * FROM users WHERE username = ?`, [username]);
        if (existing.length > 0) {
            console.log(`Error: Username '${username}' sudah digunakan! Silakan ulangi dengan username lain.`);
        } else {
            // Pastikan kolom is_demo dan status tersedia
            await connection.query(`ALTER TABLE users ADD COLUMN is_demo TINYINT(1) DEFAULT 0`).catch(e=>{});
            await connection.query(`ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'`).catch(e=>{});

            await connection.query(`
                INSERT INTO users (name, username, password, role, db_name, is_demo, status)
                VALUES (?, ?, ?, 'SUPER_ADMIN', ?, ?, 'ACTIVE')
            `, [name, username, password, dbName, isDemo]);

            // Setup user di tenant DB
            await connection.query(`USE \`${dbName}\``);
            await connection.query(`DELETE FROM users WHERE username IN ('superadmin', 'admin', 'teknisi1', 'collector1')`).catch(e=>{});
            await connection.query(`
                INSERT INTO users (name, username, password, role)
                VALUES (?, ?, ?, 'SUPER_ADMIN')
            `, [name, username, password]).catch(e=>{});

            console.log(`\n✅ Berhasil! Tenant baru '${dbName}' dengan user '${username}' (Demo: ${isDemo ? 'Ya' : 'Tidak'}) berhasil ditambahkan.`);
            console.log("Anda sekarang bisa login di aplikasi menggunakan akun tersebut.");
        }

        await connection.end();
    } catch (err) {
        console.error("\n❌ Terjadi kesalahan:", err.message);
        rl.close();
    }
}

addTenantInteractive();
