import re

filepath = 'VPS/server.js'
with open(filepath, 'r') as f:
    content = f.read()

new_endpoint = """
app.get('/api/mikrotik/secrets/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const [rows] = await pool.query('SELECT * FROM areas WHERE id = ?', [id]);
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
        
        const pppSecretMenu = api.menu('/ppp/secret');
        const allSecrets = await pppSecretMenu.get();
        
        const pppoeActiveMenu = api.menu('/ppp/active');
        const activePppoe = await pppoeActiveMenu.get();
        
        client.close();
        
        const activeNames = activePppoe.map(p => p.name);
        
        const secrets = allSecrets.map(s => {
            const isActive = activeNames.includes(s.name);
            const activeDetail = isActive ? activePppoe.find(p => p.name === s.name) : null;
            return {
                id: s['.id'],
                name: s.name,
                profile: s.profile,
                status: isActive ? "Online" : (s.disabled === 'true' ? "Disabled" : "Offline"),
                ipAddress: activeDetail ? activeDetail.address : "",
                uptime: activeDetail ? activeDetail.uptime : ""
            };
        });

        res.json(secrets);
    } catch (error) {
        console.error("Mikrotik error:", error.message);
        res.status(500).json({ error: "Failed to connect to Mikrotik: " + error.message });
    }
});
"""

content = content.replace("const PORT = 4500;", new_endpoint + "\nconst PORT = 4500;")

with open(filepath, 'w') as f:
    f.write(content)
