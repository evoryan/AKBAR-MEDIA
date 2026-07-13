import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# 1. Add additionalCost1 and additionalCost2 to ALTER TABLEs
alter_str = """
                await req.pool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(err=>{});"""

content = content.replace("""
                await req.pool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(err=>{});""", alter_str)

# Global alter table (for masterPool/tPool)
global_alter = """
        await masterPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(e=>{});"""
content = content.replace("        await masterPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});", global_alter)

global_alter_tpool = """
            await tPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(e=>{});"""
content = content.replace("            await tPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});", global_alter_tpool)

init_alter = """
            try { await pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`); } catch(e) {}"""
content = content.replace("            try { await pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`); } catch(e) {}", init_alter)


# 2. Update INSERT logic
old_insert = """            [result] = await req.pool.query(
                'INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                [name, phone, area, username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', parsedOdpId, odpPort || '']
            );"""

new_insert = """            [result] = await req.pool.query(
                'INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port, additionalCost1, additionalCost2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                [name, phone, area, username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', parsedOdpId, odpPort || '', additionalCost1 || '', additionalCost2 || '']
            );"""

content = content.replace(old_insert, new_insert)

# 3. Update UPDATE logic in PUT /api/customers/:id
# We need to find the PUT method
import re
put_pattern = r"app\.put\('/api/customers/:id', async \(req, res\) => \{.*?\};"
# Wait, maybe simpler to just replace the UPDATE query directly
with open('VPS/server.js', 'w') as f:
    f.write(content)
