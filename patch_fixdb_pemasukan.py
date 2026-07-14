import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """            try { await pool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`); } catch(e) {}
        }"""

rep = """            try { await pool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`); } catch(e) {}
            
            try {
                await pool.query(`CREATE TABLE IF NOT EXISTS pemasukan (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    category VARCHAR(100) UNIQUE,
                    amount DOUBLE,
                    description TEXT,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )`);
                results.push(`${name}: pemasukan table added`);
            } catch(e) { results.push(`${name}: pemasukan table err: ${e.message}`); }
        }"""

content = content.replace(target, rep)

with open("VPS/server.js", "w") as f:
    f.write(content)
