import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

new_endpoint = """app.post('/api/mikrotik/secrets/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, password, profile } = req.body;
        
        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        if (!area.routerIp || !area.mikrotikUser || !area.mikrotikPassword) {
            return res.status(400).json({ error: "Mikrotik credentials incomplete" });
        }
        
        const [host, port] = area.routerIp.split(':');
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        const api = await client.connect();
        
        await api.write('/ppp/secret/add', [
            `=name=${name}`,
            `=password=${password}`,
            `=profile=${profile || 'default'}`,
            `=service=pppoe`
        ]);
        
        client.close();
        res.json({ message: "Secret berhasil ditambahkan" });
    } catch (error) {
        console.error("Error adding Mikrotik secret:", error);
        res.status(500).json({ error: "Terjadi kesalahan: " + (error.message || error) });
    }
});
"""

content = content.replace("app.get('/api/mikrotik/secrets/:id', async (req, res) => {", new_endpoint + "\napp.get('/api/mikrotik/secrets/:id', async (req, res) => {")

with open('VPS/server.js', 'w') as f:
    f.write(content)
