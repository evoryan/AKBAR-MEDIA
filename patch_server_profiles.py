import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

new_endpoint = """app.get('/api/mikrotik/profiles/:id', async (req, res) => {
    try {
        const { id } = req.params;
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
        
        const pppProfileMenu = api.menu('/ppp/profile');
        const allProfiles = await pppProfileMenu.get();
        
        client.close();
        
        const profiles = allProfiles.map(p => ({
            id: p['.id'],
            name: p.name
        }));
        
        res.json(profiles);
    } catch (error) {
        console.error("Error fetching Mikrotik profiles:", error);
        res.status(500).json({ error: "Terjadi kesalahan: " + (error.message || error) });
    }
});
"""

content = content.replace("app.get('/api/mikrotik/secrets/:id', async (req, res) => {", new_endpoint + "\napp.get('/api/mikrotik/secrets/:id', async (req, res) => {")

with open('VPS/server.js', 'w') as f:
    f.write(content)
