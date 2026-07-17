import re

with open('server_updated.js', 'r') as f:
    content = f.read()

alter_table = """
            await tPool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE odc_list ADD COLUMN redaman_in VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE odc_list ADD COLUMN redaman_out VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN redaman_in VARCHAR(50) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN redaman_out VARCHAR(50) DEFAULT ''`).catch(e=>{});
"""
content = content.replace("await tPool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});\n            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});\n            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});", alter_table)

# For ODC PUT
content = content.replace("const { name, location, portCount, portInput } = req.body;", "const { name, location, portCount, portInput, redaman_in, redaman_out } = req.body;")
content = content.replace(
    "'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',",
    "'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ?, redaman_in = ?, redaman_out = ? WHERE id = ?',"
)
content = content.replace(
    "[name, location, parsedPortCount, portInput || '', id]",
    "[name, location, parsedPortCount, portInput || '', redaman_in || '', redaman_out || '', id]"
)
content = content.replace(
    "await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});",
    "await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{}); await req.pool.query(`ALTER TABLE odc_list ADD COLUMN redaman_in VARCHAR(50) DEFAULT ''`).catch(e=>{}); await req.pool.query(`ALTER TABLE odc_list ADD COLUMN redaman_out VARCHAR(50) DEFAULT ''`).catch(e=>{});"
)

# For ODP PUT
content = content.replace("const { odcId, name, portCount, portInput } = req.body;", "const { odcId, name, portCount, portInput, redaman_in, redaman_out } = req.body;")
content = content.replace(
    "'UPDATE odp_list SET name = ?, odcId = ?, portCount = ?, portInput = ? WHERE id = ?',",
    "'UPDATE odp_list SET name = ?, odcId = ?, portCount = ?, portInput = ?, redaman_in = ?, redaman_out = ? WHERE id = ?',"
)
content = content.replace(
    "[name, parsedOdcId, parsedPortCount, portInput || '', id]",
    "[name, parsedOdcId, parsedPortCount, portInput || '', redaman_in || '', redaman_out || '', id]"
)
content = content.replace(
    "await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});",
    "await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{}); await req.pool.query(`ALTER TABLE odp_list ADD COLUMN redaman_in VARCHAR(50) DEFAULT ''`).catch(e=>{}); await req.pool.query(`ALTER TABLE odp_list ADD COLUMN redaman_out VARCHAR(50) DEFAULT ''`).catch(e=>{});"
)

# For ODC POST
content = content.replace(
    "'INSERT INTO odc_list (name, location, portCount, portInput) VALUES (?, ?, ?, ?)',",
    "'INSERT INTO odc_list (name, location, portCount, portInput, redaman_in, redaman_out) VALUES (?, ?, ?, ?, ?, ?)',"
)

# For ODP POST
content = content.replace(
    "'INSERT INTO odp_list (name, odcId, portCount, portInput) VALUES (?, ?, ?, ?)',",
    "'INSERT INTO odp_list (name, odcId, portCount, portInput, redaman_in, redaman_out) VALUES (?, ?, ?, ?, ?, ?)',"
)

with open('server_updated.js', 'w') as f:
    f.write(content)
