import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Add to init_alter (inside pool connection loop)
alter_pembukuan = """
            try { await pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`); } catch(e) {}"""
content = content.replace("""
            try { await pool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await pool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`); } catch(e) {}""", alter_pembukuan)

global_alter_tpool = """
            await tPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`).catch(e=>{});"""
content = content.replace("""
            await tPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(e=>{});""", global_alter_tpool)

global_alter_master = """
        await masterPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT 'Lain-lain'`).catch(e=>{});"""
content = content.replace("""
        await masterPool.query(`ALTER TABLE customers ADD COLUMN odp_port VARCHAR(10) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN additionalCost1 VARCHAR(50) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE customers ADD COLUMN additionalCost2 VARCHAR(50) DEFAULT ''`).catch(e=>{});""", global_alter_master)


# Restore the INSERT for payment with category
old_insert = """        await req.pool.query('INSERT INTO pembukuan (type, amount, description) VALUES (?, ?, ?)', 
            ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`]);"""
new_insert = """        await req.pool.query('INSERT INTO pembukuan (type, amount, description, category) VALUES (?, ?, ?, ?)', 
            ['pemasukan', totalAmount || 0, `Pembayaran tagihan pelanggan ${customerName}`, 'Transaksi Cash']);"""
content = content.replace(old_insert, new_insert)

with open('VPS/server.js', 'w') as f:
    f.write(content)
