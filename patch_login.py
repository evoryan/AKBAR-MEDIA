import re

with open("/app/applet/VPS/server.js", "r") as f:
    content = f.read()

target = """        // Always check master database for login
        const [rows] = await masterPool.query('SELECT * FROM users WHERE username = ? AND password = ?', [username, password]);"""

rep = """        // Auto add status column if not exist
        await masterPool.query("ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'").catch(e=>{});

        // Always check master database for login
        const [rows] = await masterPool.query('SELECT * FROM users WHERE username = ? AND password = ?', [username, password]);"""

content = content.replace(target, rep)

target2 = """            const user = rows[0];
            const { password: userPassword, ...userWithoutPassword } = user;"""

rep2 = """            const user = rows[0];
            
            if (user.status === 'DISABLED') {
                return res.status(403).json({ error: "Akun/Tenant Anda dinonaktifkan. Hubungi Administrator." });
            }

            const { password: userPassword, ...userWithoutPassword } = user;"""

content = content.replace(target2, rep2)

with open("/app/applet/VPS/server.js", "w") as f:
    f.write(content)

