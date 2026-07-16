const mysql = require('mysql2/promise');
require('dotenv').config();
const fs = require('fs');

async function getConnection() {
    return await mysql.createConnection({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'akbar',
        password: process.env.DB_PASSWORD || '08Delapan',
        multipleStatements: true
    });
}

async function run() {
    const args = process.argv.slice(2);
    const action = args[0];

    if (!action) return;

    try {
        const conn = await getConnection();
        await conn.query(`USE akbar_media_master`);
        await conn.query("ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'").catch(e=>{});

        if (action === 'list') {
            console.log("\n--- Daftar Tenant ---");
            const [users] = await conn.query("SELECT id, name, username, db_name, status FROM users WHERE role = 'SUPER_ADMIN'");
            if (users.length === 0) {
                console.log("Belum ada tenant.");
            } else {
                console.table(users);
            }
            console.log("---------------------\n");
        } 
        else if (action === 'add') {
            const dbName = args[1];
            const name = args[2];
            const username = args[3];
            const password = args[4];

            console.log(`\n[1/3] Membuat database tenant: ${dbName}...`);
            await conn.query(`CREATE DATABASE IF NOT EXISTS \`${dbName}\``);

            console.log(`[2/3] Menginisialisasi tabel-tabel pada database ${dbName}...`);
            let initSql = fs.readFileSync('init.sql', 'utf8');
            initSql = initSql.replace(/CREATE DATABASE IF NOT EXISTS app_db;/g, '');
            initSql = initSql.replace(/USE app_db;/g, '');
            await conn.query(`USE \`${dbName}\``);
            await conn.query(initSql);

            console.log(`[3/3] Menambahkan akun superadmin '${username}' ke master database...`);
            await conn.query(`USE akbar_media_master`);
            
            const [existing] = await conn.query(`SELECT * FROM users WHERE username = ?`, [username]);
            if (existing.length > 0) {
                console.log(`❌ Error: Username '${username}' sudah digunakan!`);
            } else {
                await conn.query(`
                    INSERT INTO users (name, username, password, role, db_name, status)
                    VALUES (?, ?, ?, 'SUPER_ADMIN', ?, 'ACTIVE')
                `, [name, username, password, dbName]);
                
                console.log(`[4/4] Menambahkan akun superadmin '${username}' ke database tenant...`);
                await conn.query(`USE \`${dbName}\``);
                // Hapus user default bawaan init.sql (opsional tapi disarankan agar bersih)
                await conn.query(`DELETE FROM users WHERE username IN ('superadmin', 'admin', 'teknisi1', 'collector1')`);
                // Tambahkan user yang baru dibuat
                await conn.query(`
                    INSERT INTO users (name, username, password, role)
                    VALUES (?, ?, ?, 'SUPER_ADMIN')
                `, [name, username, password]);

                console.log(`✅ Berhasil! Tenant baru '${dbName}' dengan user '${username}' berhasil ditambahkan.`);
            }
        }
        else if (action === 'delete') {
            const username = args[1];
            const [users] = await conn.query("SELECT * FROM users WHERE username = ?", [username]);
            if (users.length === 0) {
                console.log(`❌ User '${username}' tidak ditemukan.`);
            } else {
                const user = users[0];
                console.log(`Menghapus database ${user.db_name}...`);
                await conn.query(`DROP DATABASE IF EXISTS \`${user.db_name}\``).catch(e=>console.log(e.message));
                console.log(`Menghapus user ${username}...`);
                await conn.query(`DELETE FROM users WHERE username = ?`, [username]);
                console.log("✅ Tenant berhasil dihapus.");
            }
        }
        else if (action === 'disable' || action === 'enable') {
            const username = args[1];
            const targetStatus = action === 'disable' ? 'DISABLED' : 'ACTIVE';
            const [users] = await conn.query("SELECT * FROM users WHERE username = ?", [username]);
            if (users.length === 0) {
                console.log(`❌ User '${username}' tidak ditemukan.`);
            } else {
                await conn.query(`UPDATE users SET status = ? WHERE username = ?`, [targetStatus, username]);
                console.log(`✅ Tenant untuk user '${username}' berhasil di-${action} (Status: ${targetStatus}).`);
            }
        }
        
        await conn.end();
    } catch (e) {
        console.error("Terjadi kesalahan:", e.message);
    }
}

run();
