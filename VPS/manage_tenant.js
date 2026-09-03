const mysql = require('mysql2/promise');
require('dotenv').config();
const fs = require('fs');
const path = require('path');

async function getConnection() {
    return await mysql.createConnection({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'akbar',
        password: process.env.DB_PASSWORD || '08Delapan',
        multipleStatements: true
    });
}

async function executeSqlFile(conn, filePath, targetDb) {
    if (!fs.existsSync(filePath)) {
        console.log(`⚠️ Peringatan: File '${filePath}' tidak ditemukan.`);
        return;
    }
    await conn.query(`USE \`${targetDb}\``);
    const rawSql = fs.readFileSync(filePath, 'utf8');

    // Hapus baris komentar dan block komentar
    const noComments = rawSql
        .replace(/\/\*[\s\S]*?\*\//g, '')
        .replace(/^[ \t]*--.*$/gm, '')
        .replace(/^[ \t]*#.*$/gm, '');

    // Pecah menjadi statement terpisah berdasarkan titik koma (;)
    const statements = noComments
        .split(';')
        .map(s => s.trim())
        .filter(s => s.length > 0);

    for (const stmt of statements) {
        // Lewati statement CREATE DATABASE dan USE bawaan template
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

async function run() {
    const args = process.argv.slice(2);
    const action = args[0];

    if (!action) {
        console.log("Usage: node manage_tenant.js <list|add|delete|disable|enable|reset-password|toggle-demo|stats> [params...]");
        return;
    }

    let conn;
    try {
        conn = await getConnection();
        await conn.query(`USE akbar_media_master`);
        await conn.query("ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE'").catch(e=>{});
        await conn.query("ALTER TABLE users ADD COLUMN IF NOT EXISTS is_demo TINYINT(1) DEFAULT 0").catch(e=>{});

        if (action === 'list') {
            console.log("\n=======================================================");
            console.log("                 DAFTAR SEMUA TENANT                   ");
            console.log("=======================================================");
            const [users] = await conn.query("SELECT id, name, username, db_name, role, status, is_demo, created_at FROM users ORDER BY id ASC");
            if (users.length === 0) {
                console.log("Belum ada data tenant terdaftar.");
            } else {
                const formatted = [];
                for (const u of users) {
                    let totalCustomers = "-";
                    let totalUsers = "-";
                    let dbExists = "✅ Ada";

                    try {
                        const [cRows] = await conn.query(`SELECT COUNT(*) as count FROM \`${u.db_name}\`.customers`);
                        totalCustomers = cRows[0].count;
                        const [uRows] = await conn.query(`SELECT COUNT(*) as count FROM \`${u.db_name}\`.users`);
                        totalUsers = uRows[0].count;
                    } catch (err) {
                        dbExists = "❌ Tidak Ditemukan";
                    }

                    formatted.push({
                        ID: u.id,
                        Tenant: u.name,
                        Username: u.username,
                        Database: u.db_name,
                        Status: u.status || 'ACTIVE',
                        Demo: u.is_demo === 1 ? 'YA (Demo)' : 'TIDAK',
                        DB_Status: dbExists,
                        Pelanggan: totalCustomers,
                        User_App: totalUsers
                    });
                }
                console.table(formatted);
            }
            console.log("=======================================================\n");
        } 
        else if (action === 'add') {
            const dbName = args[1];
            const name = args[2];
            const username = args[3];
            const password = args[4];
            const isDemo = args[5] === '1' || args[5] === 'true' || args[5] === 'y' ? 1 : 0;

            if (!dbName || !name || !username || !password) {
                console.log("❌ Error: Parameter tidak lengkap. Dibutuhkan: dbName, name, username, password, [isDemo]");
                return;
            }

            console.log(`\n[1/4] Membuat database tenant: \`${dbName}\`...`);
            await conn.query(`CREATE DATABASE IF NOT EXISTS \`${dbName}\``);

            console.log(`[2/4] Menginisialisasi tabel-tabel pada database \`${dbName}\`...`);
            const initSqlPath = path.join(__dirname, 'init.sql');
            await executeSqlFile(conn, initSqlPath, dbName);

            console.log(`[3/4] Menambahkan akun superadmin '${username}' ke master database...`);
            await conn.query(`USE akbar_media_master`);
            
            const [existing] = await conn.query(`SELECT * FROM users WHERE username = ?`, [username]);
            if (existing.length > 0) {
                console.log(`❌ Error: Username '${username}' sudah digunakan! Silakan gunakan username lain.`);
                return;
            }

            await conn.query(`
                INSERT INTO users (name, username, password, role, db_name, status, is_demo)
                VALUES (?, ?, ?, 'SUPER_ADMIN', ?, 'ACTIVE', ?)
            `, [name, username, password, dbName, isDemo]);
            
            console.log(`[4/4] Menyiapkan akun superadmin '${username}' di database tenant \`${dbName}\`...`);
            await conn.query(`USE \`${dbName}\``);
            await conn.query(`DELETE FROM users WHERE username IN ('superadmin', 'admin', 'teknisi1', 'collector1')`).catch(e=>{});
            await conn.query(`
                INSERT INTO users (name, username, password, role)
                VALUES (?, ?, ?, 'SUPER_ADMIN')
            `, [name, username, password]).catch(e=>{});

            console.log(`\n✅ SUKSES! Tenant baru '${name}' [${dbName}] dengan username '${username}' berhasil ditambahkan.`);
            console.log(`   Status: ACTIVE | Demo Mode: ${isDemo ? 'YA' : 'TIDAK'}`);
        }
        else if (action === 'delete') {
            const username = args[1];
            if (!username) {
                console.log("❌ Error: Harap masukkan username tenant yang ingin dihapus.");
                return;
            }

            const [users] = await conn.query("SELECT * FROM users WHERE username = ?", [username]);
            if (users.length === 0) {
                console.log(`❌ Error: User '${username}' tidak ditemukan.`);
                return;
            }

            const user = users[0];
            if (user.db_name === 'akbar_media_master') {
                console.log("❌ Error: Tidak dapat menghapus master database!");
                return;
            }

            console.log(`Menghapus database tenant \`${user.db_name}\`...`);
            await conn.query(`DROP DATABASE IF EXISTS \`${user.db_name}\``).catch(e => console.log(`   Peringatan drop db: ${e.message}`));

            console.log(`Menghapus user '${username}' dari master database...`);
            await conn.query(`DELETE FROM users WHERE username = ?`, [username]);

            console.log(`✅ SUKSES: Tenant '${user.name}' [${user.db_name}] berhasil dihapus sepenuhnya.`);
        }
        else if (action === 'disable' || action === 'enable') {
            const username = args[1];
            if (!username) {
                console.log("❌ Error: Harap masukkan username tenant.");
                return;
            }

            const targetStatus = action === 'disable' ? 'DISABLED' : 'ACTIVE';
            const [users] = await conn.query("SELECT * FROM users WHERE username = ?", [username]);
            if (users.length === 0) {
                console.log(`❌ Error: User '${username}' tidak ditemukan.`);
                return;
            }

            await conn.query(`UPDATE users SET status = ? WHERE username = ?`, [targetStatus, username]);
            console.log(`✅ SUKSES: Status tenant untuk user '${username}' diubah menjadi: ${targetStatus}.`);
        }
        else if (action === 'reset-password') {
            const username = args[1];
            const newPassword = args[2];
            if (!username || !newPassword) {
                console.log("❌ Error: Harap masukkan username dan password baru.");
                return;
            }

            const [users] = await conn.query("SELECT * FROM users WHERE username = ?", [username]);
            if (users.length === 0) {
                console.log(`❌ Error: User '${username}' tidak ditemukan.`);
                return;
            }

            const user = users[0];
            await conn.query(`UPDATE users SET password = ? WHERE username = ?`, [newPassword, username]);

            // Update juga di database tenant jika DB ada
            try {
                await conn.query(`USE \`${user.db_name}\``);
                await conn.query(`UPDATE users SET password = ? WHERE username = ?`, [newPassword, username]);
            } catch (err) {}

            console.log(`✅ SUKSES: Password untuk tenant '${username}' berhasil diubah!`);
        }
        else if (action === 'toggle-demo') {
            const username = args[1];
            if (!username) {
                console.log("❌ Error: Harap masukkan username tenant.");
                return;
            }

            const [users] = await conn.query("SELECT * FROM users WHERE username = ?", [username]);
            if (users.length === 0) {
                console.log(`❌ Error: User '${username}' tidak ditemukan.`);
                return;
            }

            const current = users[0].is_demo === 1 ? 1 : 0;
            const nextVal = current === 1 ? 0 : 1;
            await conn.query(`UPDATE users SET is_demo = ? WHERE username = ?`, [nextVal, username]);
            console.log(`✅ SUKSES: Mode Demo untuk user '${username}' sekarang: ${nextVal === 1 ? 'AKTIF (Demo)' : 'NONAKTIF (Produksi)'}.`);
        }
        else if (action === 'stats') {
            const username = args[1];
            if (!username) {
                console.log("❌ Error: Harap masukkan username tenant.");
                return;
            }

            const [users] = await conn.query("SELECT * FROM users WHERE username = ?", [username]);
            if (users.length === 0) {
                console.log(`❌ Error: User '${username}' tidak ditemukan.`);
                return;
            }

            const u = users[0];
            console.log(`\n=======================================================`);
            console.log(`                 STATISTIK TENANT                     `);
            console.log(`=======================================================`);
            console.log(` Nama Tenant    : ${u.name}`);
            console.log(` Username       : ${u.username}`);
            console.log(` Database       : ${u.db_name}`);
            console.log(` Status Login   : ${u.status || 'ACTIVE'}`);
            console.log(` Mode Demo      : ${u.is_demo === 1 ? 'YA' : 'TIDAK'}`);
            console.log(` Dibuat Pada    : ${u.created_at || '-'}`);
            console.log(`-------------------------------------------------------`);

            try {
                await conn.query(`USE \`${u.db_name}\``);
                const [cust] = await conn.query("SELECT COUNT(*) as count FROM customers").catch(()=>[[{count: 0}]]);
                const [areas] = await conn.query("SELECT COUNT(*) as count FROM areas").catch(()=>[[{count: 0}]]);
                const [trans] = await conn.query("SELECT COUNT(*) as count FROM transactions").catch(()=>[[{count: 0}]]);
                const [inv] = await conn.query("SELECT COUNT(*) as count FROM invoices").catch(()=>[[{count: 0}]]);
                const [usrs] = await conn.query("SELECT COUNT(*) as count FROM users").catch(()=>[[{count: 0}]]);

                console.log(` 👥 Total Pelanggan : ${cust[0].count}`);
                console.log(` 🌐 Total Area/Router: ${areas[0].count}`);
                console.log(` 💰 Total Transaksi : ${trans[0].count}`);
                console.log(` 📄 Total Tagihan   : ${inv[0].count}`);
                console.log(` 👤 Total User Akun : ${usrs[0].count}`);
            } catch (err) {
                console.log(` ⚠️ Database \`${u.db_name}\` tidak dapat diakses atau belum diinisialisasi.`);
            }
            console.log(`=======================================================\n`);
        }
        else {
            console.log(`Aksi '${action}' tidak dikenal.`);
        }
    } catch (e) {
        console.error("❌ Terjadi kesalahan:", e.message);
    } finally {
        if (conn) await conn.end();
    }
}

run();
