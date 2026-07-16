import re

with open("VPS/server.js", "r") as f:
    content = f.read()

pattern_add = r"""app\.post\('/api/customers', async \(req, res\) => \{
    try \{
        const \{ name, phone, area, username, billingDate, status, price, discount, additionalCost1, additionalCost2 \} = req\.body;"""
replacement_add = """app.post('/api/customers', async (req, res) => {
    try {
        const { name, phone, area, address, username, billingDate, status, price, discount, additionalCost1, additionalCost2 } = req.body;"""

content = re.sub(pattern_add, replacement_add, content)

pattern_ins1 = r"""                'INSERT INTO customers \(name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port, additionalCost1, additionalCost2\) VALUES \(\?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?\)',
                \[name, phone, area, username, billingDate, status, price, discount, registerDate \|\| '', isolateDate \|\| '', packageName \|\| '', pppoeSecret \|\| '', parsedOdpId, odpPort \|\| '', additionalCost1 \|\| '', additionalCost2 \|\| ''\]"""
replacement_ins1 = """                'INSERT INTO customers (name, phone, area, address, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port, additionalCost1, additionalCost2) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                [name, phone, area, address || '', username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', parsedOdpId, odpPort || '', additionalCost1 || '', additionalCost2 || '']"""

content = re.sub(pattern_ins1, replacement_ins1, content)

pattern_catch = r"""            if \(e\.message && e\.message\.includes\("Unknown column"\)\) \{
                await req\.pool\.query\(`ALTER TABLE customers ADD COLUMN register_date VARCHAR\(50\) DEFAULT ''`\)\.catch\(err=>\{\}\);"""
replacement_catch = """            if (e.message && e.message.includes("Unknown column")) {
                await req.pool.query(`ALTER TABLE customers ADD COLUMN address TEXT`).catch(err=>{});
                await req.pool.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`).catch(err=>{});"""
content = re.sub(pattern_catch, replacement_catch, content)

pattern_ins2 = r"""                \[result\] = await req\.pool\.query\(
                    'INSERT INTO customers \(name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port\) VALUES \(\?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?, \?\)',
                    \[name, phone, area, username, billingDate, status, price, discount, registerDate \|\| '', isolateDate \|\| '', packageName \|\| '', pppoeSecret \|\| '', parsedOdpId, odpPort \|\| ''\]
                \);"""
replacement_ins2 = """                [result] = await req.pool.query(
                    'INSERT INTO customers (name, phone, area, address, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
                    [name, phone, area, address || '', username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', parsedOdpId, odpPort || '']
                );"""
content = re.sub(pattern_ins2, replacement_ins2, content)

# Modify init.sql to include address
with open("VPS/init.sql", "r") as f:
    init_sql = f.read()

init_sql = init_sql.replace("    area VARCHAR(50),\n    username VARCHAR(50)", "    area VARCHAR(50),\n    address TEXT,\n    username VARCHAR(50)")

with open("VPS/init.sql", "w") as f:
    f.write(init_sql)

with open("VPS/server.js", "w") as f:
    f.write(content)

