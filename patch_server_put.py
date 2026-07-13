import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Add PUT /api/odc/:id
put_odc = """app.put('/api/odc/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, location, portCount, portInput } = req.body;
        await req.pool.query(
            'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',
            [name, location, portCount || 0, portInput || '', id]
        );
        res.json({ message: "ODC diupdate" });
    } catch (error) {
        res.status(500).json({ error: "Terjadi kesalahan" });
    }
});

app.put('/api/odp/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { odcId, name, portCount, portInput } = req.body;
        await req.pool.query(
            'UPDATE odp_list SET odcId = ?, name = ?, portCount = ?, portInput = ? WHERE id = ?',
            [odcId, name, portCount || 0, portInput || '', id]
        );
        res.json({ message: "ODP diupdate" });
    } catch (error) {
        res.status(500).json({ error: "Terjadi kesalahan" });
    }
});
"""

content = content.replace("app.delete('/api/odc/:id', async (req, res) => {", put_odc + "\napp.delete('/api/odc/:id', async (req, res) => {")

with open('VPS/server.js', 'w') as f:
    f.write(content)
