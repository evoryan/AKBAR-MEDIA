import re

with open("VPS/manage_tenant.js", "r") as f:
    content = f.read()

pattern = r"""            const \[existing\] = await conn\.query\(`SELECT \* FROM users WHERE username = \?`, \[username\]\);
            if \(existing\.length > 0\) \{
                console\.log\(`❌ Error: Username '\$\{username\}' sudah digunakan!`\);
            \} else \{
                await conn\.query\(`
                    INSERT INTO users \(name, username, password, role, db_name, status\)
                    VALUES \(\?, \?, \?, 'SUPER_ADMIN', \?, 'ACTIVE'\)
                `, \[name, username, password, dbName\]\);
                console\.log\(`✅ Berhasil! Tenant baru '\$\{dbName\}' dengan user '\$\{username\}' berhasil ditambahkan\.`\);
            \}"""

replacement = """            const [existing] = await conn.query(`SELECT * FROM users WHERE username = ?`, [username]);
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
            }"""

content = re.sub(pattern, replacement, content)

with open("VPS/manage_tenant.js", "w") as f:
    f.write(content)
