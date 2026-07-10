with open('VPS/server.js', 'r') as f:
    content = f.read()

pppoe_endpoint_old = """
app.get('/api/dashboard/pppoe-offline', async (req, res) => {
    try {
        const [areas] = await pool.query('SELECT * FROM areas');
        let totalOffline = 0;
        
        for (const area of areas) {
            if (!area.routerIp || !area.mikrotikUser || !area.mikrotikPassword) continue;
            try {
                const [host, portStr] = area.routerIp.split(':');
                const port = parseInt(portStr) || 8728;
                const client = new RouterOSClient({
                    host, user: area.mikrotikUser, password: area.mikrotikPassword, port, timeout: 3000
                });
                const api = await client.connect();
                
                const activeMenu = api.menu('/ppp/active');
                const actives = await activeMenu.get();
                
                const secretMenu = api.menu('/ppp/secret');
                const secrets = await secretMenu.get();
                
                const activeNames = new Set(actives.map(a => a.name));
                const offline = secrets.filter(s => !activeNames.has(s.name) && s.disabled !== 'true');
                totalOffline += offline.length;
                
                client.close();
            } catch (err) {
                console.error(`Error connecting to mikrotik for area ${area.name}:`, err.message);
            }
        }
        
        res.json({ offlinePPPoE: totalOffline });
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});
"""

pppoe_endpoint_new = """
app.get('/api/dashboard/pppoe-offline', async (req, res) => {
    try {
        const [areas] = await pool.query('SELECT * FROM areas');
        let offlineList = [];
        
        for (const area of areas) {
            if (!area.routerIp || !area.mikrotikUser || !area.mikrotikPassword) continue;
            try {
                const [host, portStr] = area.routerIp.split(':');
                const port = parseInt(portStr) || 8728;
                const client = new RouterOSClient({
                    host, user: area.mikrotikUser, password: area.mikrotikPassword, port, timeout: 3000
                });
                const api = await client.connect();
                
                const activeMenu = api.menu('/ppp/active');
                const actives = await activeMenu.get();
                
                const secretMenu = api.menu('/ppp/secret');
                const secrets = await secretMenu.get();
                
                const activeNames = new Set(actives.map(a => a.name));
                const offline = secrets.filter(s => !activeNames.has(s.name) && s.disabled !== 'true');
                
                offline.forEach(o => {
                    offlineList.push({ name: o.name, lastLogoff: o['last-logged-out'] || 'Unknown', area: area.name });
                });
                
                client.close();
            } catch (err) {
                console.error(`Error connecting to mikrotik for area ${area.name}:`, err.message);
            }
        }
        
        res.json(offlineList);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});
"""

content = content.replace(pppoe_endpoint_old, pppoe_endpoint_new)
with open('VPS/server.js', 'w') as f:
    f.write(content)
