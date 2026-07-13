import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

odc_put = """app.put('/api/odc/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, location, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        try {
            await req.pool.query(
                'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',
                [name, location, parsedPortCount, portInput || '', id]
            );
        } catch(e) {
            if (e.message && (e.message.includes("Unknown column") || e.message.includes("Unknown column 'portinput'"))) {
                await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(err=>{});
                await req.pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(
                    'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',
                    [name, location, parsedPortCount, portInput || '', id]
                );
            } else {
                throw e;
            }
        }
        res.json({ message: "ODC diupdate" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });
    }
});"""

content = re.sub(r"app\.put\('/api/odc/:id', async \(req, res\) => \{.*?\n\}\);", odc_put, content, flags=re.DOTALL)

odp_put = """app.put('/api/odp/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { odcId, name, portCount, portInput } = req.body;
        const parsedOdcId = parseInt(odcId) || 0;
        const parsedPortCount = parseInt(portCount) || 0;
        try {
            await req.pool.query(
                'UPDATE odp_list SET name = ?, odcId = ?, portCount = ?, portInput = ? WHERE id = ?',
                [name, parsedOdcId, parsedPortCount, portInput || '', id]
            );
        } catch(e) {
            if (e.message && (e.message.includes("Unknown column") || e.message.includes("Unknown column 'portinput'"))) {
                await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(err=>{});
                await req.pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(err=>{});
                await req.pool.query(
                    'UPDATE odp_list SET name = ?, odcId = ?, portCount = ?, portInput = ? WHERE id = ?',
                    [name, parsedOdcId, parsedPortCount, portInput || '', id]
                );
            } else {
                throw e;
            }
        }
        res.json({ message: "ODP diupdate" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });
    }
});"""

content = re.sub(r"app\.put\('/api/odp/:id', async \(req, res\) => \{.*?\n\}\);", odp_put, content, flags=re.DOTALL)

with open('VPS/server.js', 'w') as f:
    f.write(content)
