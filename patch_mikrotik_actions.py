import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Replace disable route
disable_pattern = re.compile(r"app\.post\('/api/mikrotik/secrets/:id/disable'.*?res\.status\(500\)\.json\(\{ error: \"Gagal mendisable secret: \" \+ error\.message \}\);\n    \}\n\}\);", re.DOTALL)

disable_new = """app.post('/api/mikrotik/secrets/:id/disable', async (req, res) => {
    try {
        const { id } = req.params;
        const secretName = req.body.secretName || req.body.secretId;
        
        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        let host = area.routerIp;
        let port = 8728;
        if (host.includes(':')) {
            const parts = host.split(':');
            host = parts[0];
            port = parseInt(parts[1]);
        }
        
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        await client.connect();
        const secretMenu = client.menu('/ppp/secret');
        await secretMenu.where({ name: secretName }).update({ disabled: "yes" });
        
        client.close();
        res.json({ message: "Secret berhasil di-disable" });
    } catch (error) {
        console.error("Error disabling Mikrotik secret:", error);
        res.status(500).json({ error: "Gagal mendisable secret: " + error.message });
    }
});"""

content = disable_pattern.sub(disable_new, content)

# Replace enable route
enable_pattern = re.compile(r"app\.post\('/api/mikrotik/secrets/:id/enable'.*?res\.status\(500\)\.json\(\{ error: \"Gagal meng-enable secret: \" \+ error\.message \}\);\n    \}\n\}\);", re.DOTALL)

enable_new = """app.post('/api/mikrotik/secrets/:id/enable', async (req, res) => {
    try {
        const { id } = req.params;
        const secretName = req.body.secretName || req.body.secretId;
        
        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        let host = area.routerIp;
        let port = 8728;
        if (host.includes(':')) {
            const parts = host.split(':');
            host = parts[0];
            port = parseInt(parts[1]);
        }
        
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        await client.connect();
        const secretMenu = client.menu('/ppp/secret');
        await secretMenu.where({ name: secretName }).update({ disabled: "no" });
        
        client.close();
        res.json({ message: "Secret berhasil di-enable" });
    } catch (error) {
        console.error("Error enabling Mikrotik secret:", error);
        res.status(500).json({ error: "Gagal meng-enable secret: " + error.message });
    }
});"""

content = enable_pattern.sub(enable_new, content)

# Replace remove active route
remove_pattern = re.compile(r"app\.post\('/api/mikrotik/secrets/:id/remove-active'.*?res\.status\(500\)\.json\(\{ error: \"Gagal meremove active connection: \" \+ error\.message \}\);\n    \}\n\}\);", re.DOTALL)

remove_new = """app.post('/api/mikrotik/secrets/:id/remove-active', async (req, res) => {
    try {
        const { id } = req.params;
        const secretName = req.body.secretName || req.body.secretId;
        
        const [rows] = await req.pool.query('SELECT * FROM areas WHERE id = ?', [id]);
        if (rows.length === 0) return res.status(404).json({ error: "Area not found" });
        const area = rows[0];
        
        let host = area.routerIp;
        let port = 8728;
        if (host.includes(':')) {
            const parts = host.split(':');
            host = parts[0];
            port = parseInt(parts[1]);
        }
        
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        await client.connect();
        const activeMenu = client.menu('/ppp/active');
        await activeMenu.where({ name: secretName }).remove();
        
        client.close();
        res.json({ message: "Active connection berhasil diremove" });
    } catch (error) {
        console.error("Error removing active connection:", error);
        res.status(500).json({ error: "Gagal meremove active connection: " + error.message });
    }
});"""

content = remove_pattern.sub(remove_new, content)

with open("VPS/server.js", "w") as f:
    f.write(content)

