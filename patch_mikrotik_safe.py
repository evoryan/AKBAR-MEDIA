import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Replace disable route
disable_pattern = re.compile(r"app\.post\('/api/mikrotik/secrets/:id/disable', async \(req, res\) => \{.*?(?=\napp\.post\('/api/mikrotik/secrets/:id/enable')", re.DOTALL)
disable_repl = """app.post('/api/mikrotik/secrets/:id/disable', async (req, res) => {
    try {
        const { id } = req.params;
        const identifier = req.body.secretId || req.body.secretName;
        
        if (!identifier) return res.status(400).json({ error: "secretId is required" });

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
        
        // Find exact item safely
        let realId = null;
        let results = await client.rosApi.write('/ppp/secret/print', [`?.id=${identifier}`]);
        if (results.length > 0) {
            realId = results[0]['.id'];
        } else {
            results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
            if (results.length > 0) realId = results[0]['.id'];
        }

        if (!realId) {
            client.close();
            return res.status(404).json({ error: "Secret not found in Mikrotik" });
        }
        
        await client.rosApi.write('/ppp/secret/set', [
            `=.id=${realId}`,
            `=disabled=yes`
        ]);
        
        client.close();
        res.json({ message: "Secret berhasil di-disable" });
    } catch (error) {
        console.error("Error disabling Mikrotik secret:", error);
        res.status(500).json({ error: "Gagal mendisable secret: " + error.message });
    }
});"""
content = disable_pattern.sub(disable_repl, content)

# Replace enable route
enable_pattern = re.compile(r"app\.post\('/api/mikrotik/secrets/:id/enable', async \(req, res\) => \{.*?(?=\napp\.post\('/api/mikrotik/secrets/:id/remove-active')", re.DOTALL)
enable_repl = """app.post('/api/mikrotik/secrets/:id/enable', async (req, res) => {
    try {
        const { id } = req.params;
        const identifier = req.body.secretId || req.body.secretName;
        
        if (!identifier) return res.status(400).json({ error: "secretId is required" });

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
        
        let realId = null;
        let results = await client.rosApi.write('/ppp/secret/print', [`?.id=${identifier}`]);
        if (results.length > 0) {
            realId = results[0]['.id'];
        } else {
            results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
            if (results.length > 0) realId = results[0]['.id'];
        }

        if (!realId) {
            client.close();
            return res.status(404).json({ error: "Secret not found in Mikrotik" });
        }
        
        await client.rosApi.write('/ppp/secret/set', [
            `=.id=${realId}`,
            `=disabled=no`
        ]);
        
        client.close();
        res.json({ message: "Secret berhasil di-enable" });
    } catch (error) {
        console.error("Error enabling Mikrotik secret:", error);
        res.status(500).json({ error: "Gagal meng-enable secret: " + error.message });
    }
});"""
content = enable_pattern.sub(enable_repl, content)

# Replace remove active route
remove_pattern = re.compile(r"app\.post\('/api/mikrotik/secrets/:id/remove-active', async \(req, res\) => \{.*?(?=\napp\.get\('/api/mikrotik/secrets/:id')", re.DOTALL)
remove_repl = """app.post('/api/mikrotik/secrets/:id/remove-active', async (req, res) => {
    try {
        const { id } = req.params;
        const identifier = req.body.secretName || req.body.secretId;
        
        if (!identifier) return res.status(400).json({ error: "secretName is required" });

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
        
        let activeId = null;
        let results = await client.rosApi.write('/ppp/active/print', [`?name=${identifier}`]);
        if (results.length > 0) {
            activeId = results[0]['.id'];
        } else {
            results = await client.rosApi.write('/ppp/active/print', [`?.id=${identifier}`]);
            if (results.length > 0) activeId = results[0]['.id'];
        }

        if (activeId) {
            await client.rosApi.write('/ppp/active/remove', [
                `=.id=${activeId}`
            ]);
            client.close();
            return res.json({ message: "Active connection berhasil diremove" });
        } else {
            client.close();
            return res.status(404).json({ error: "Active connection tidak ditemukan" });
        }
    } catch (error) {
        console.error("Error removing active connection:", error);
        res.status(500).json({ error: "Gagal meremove active connection: " + error.message });
    }
});"""
content = remove_pattern.sub(remove_repl, content)

with open("VPS/server.js", "w") as f:
    f.write(content)

