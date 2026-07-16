import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """// Ensure table exists
        await req.pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan ("""

rep = """// Ensure table exists
        await req.pool.query(`ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS category VARCHAR(100)`).catch(e=>{});
        await req.pool.query(`ALTER TABLE pembukuan ADD COLUMN IF NOT EXISTS admin_name VARCHAR(100)`).catch(e=>{});
        
        await req.pool.query(`CREATE TABLE IF NOT EXISTS tagihan_bulanan ("""

content = content.replace(target, rep)

with open("VPS/server.js", "w") as f:
    f.write(content)
