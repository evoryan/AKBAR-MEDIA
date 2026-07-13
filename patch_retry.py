import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

odc_post = """app.post('/api/odc', async (req, res) => {
    try {
        const { name, location, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        let result;
        try {
            [result] = await req.pool.query(
                'INSERT INTO odc_list (name, location, portCount, portInput) VALUES (?, ?, ?, ?)',
                [name, location, parsedPortCount, portInput || '']
            );
        } catch(e) {
            if (e.message && (e.message.includes("Unknown column") || e.message.includes("Unknown column 'portinput'"))) {
                await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(err=>{});
                await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});
                [result] = await req.pool.query(
                    'INSERT INTO odc_list (name, location, portCount, portInput) VALUES (?, ?, ?, ?)',
                    [name, location, parsedPortCount, portInput || '']
                );
            } else {
                throw e;
            }
        }
        res.json({ message: "ODC ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });
    }
});"""

content = re.sub(r"app\.post\('/api/odc', async \(req, res\) => \{.*?\n\}\);", odc_post, content, flags=re.DOTALL)

odp_post = """app.post('/api/odp', async (req, res) => {
    try {
        const { odcId, name, portCount, portInput } = req.body;
        const parsedOdcId = parseInt(odcId) || 0;
        const parsedPortCount = parseInt(portCount) || 0;
        let result;
        try {
            [result] = await req.pool.query(
                'INSERT INTO odp_list (name, odcId, portCount, portInput) VALUES (?, ?, ?, ?)',
                [name, parsedOdcId, parsedPortCount, portInput || '']
            );
        } catch(e) {
            if (e.message && (e.message.includes("Unknown column") || e.message.includes("Unknown column 'portinput'"))) {
                await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(err=>{});
                await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});
                [result] = await req.pool.query(
                    'INSERT INTO odp_list (name, odcId, portCount, portInput) VALUES (?, ?, ?, ?)',
                    [name, parsedOdcId, parsedPortCount, portInput || '']
                );
            } else {
                throw e;
            }
        }
        res.json({ message: "ODP ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });
    }
});"""

content = re.sub(r"app\.post\('/api/odp', async \(req, res\) => \{.*?\n\}\);", odp_post, content, flags=re.DOTALL)

with open('VPS/server.js', 'w') as f:
    f.write(content)
