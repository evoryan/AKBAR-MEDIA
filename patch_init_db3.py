import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# For /api/fix-db
fix_db_part = """
            try {
                await pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`);
                results.push(`${name}: odp_list portInput added`);
            } catch(e) { results.push(`${name}: odp_list portInput err: ${e.message}`); }
"""
fix_db_addition = """
            try { await pool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`); } catch(e) {}
"""
content = content.replace(fix_db_part, fix_db_part + fix_db_addition)

# For initAllDatabases master
init_master_part = """
        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
"""
init_master_addition = """
        await masterPool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
"""
content = content.replace(init_master_part, init_master_part + init_master_addition)

# For initAllDatabases tenant
init_tenant_part = """
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
"""
init_tenant_addition = """
            await tPool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN pppoe_secret VARCHAR(100) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN odp_id INT DEFAULT NULL`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
"""
content = content.replace(init_tenant_part, init_tenant_part + init_tenant_addition)

with open('VPS/server.js', 'w') as f:
    f.write(content)
