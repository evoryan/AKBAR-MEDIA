import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

fix_db_route = """
app.get('/api/fix-db', async (req, res) => {
    try {
        let results = [];
        const pools = { master: masterPool, ...tenantPools };
        for (const [name, pool] of Object.entries(pools)) {
            try {
                await pool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`);
                results.push(`${name}: odc_list portCount added`);
            } catch(e) { results.push(`${name}: odc_list portCount err: ${e.message}`); }
            
            try {
                await pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`);
                results.push(`${name}: odc_list portInput added`);
            } catch(e) { results.push(`${name}: odc_list portInput err: ${e.message}`); }
            
            try {
                await pool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`);
                results.push(`${name}: odp_list portCount added`);
            } catch(e) { results.push(`${name}: odp_list portCount err: ${e.message}`); }
            
            try {
                await pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`);
                results.push(`${name}: odp_list portInput added`);
            } catch(e) { results.push(`${name}: odp_list portInput err: ${e.message}`); }
        }
        res.json({ message: "Database schema check completed", details: results });
    } catch(e) {
        res.status(500).json({ error: e.message });
    }
});
"""

content = content.replace("app.use(tenantContext);", fix_db_route + "\napp.use(tenantContext);")

with open('VPS/server.js', 'w') as f:
    f.write(content)
