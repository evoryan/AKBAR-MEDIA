import re

filepath = 'VPS/server.js'
with open(filepath, 'r') as f:
    content = f.read()

# Add routeros-client require at the top
content = content.replace("const express = require('express');", "const express = require('express');\nconst { RouterOSClient } = require('routeros-client');")

mikrotik_endpoint = """
app.get('/api/mikrotik/status/:id', async (req, res) => {
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

        await client.connect();

        const resourceMenu = client.menu('/system/resource');
        const resources = await resourceMenu.get();
        const resource = resources[0];

        const pppoeActiveMenu = client.menu('/ppp/active');
        const activePppoe = await pppoeActiveMenu.get();
        
        const pppSecretMenu = client.menu('/ppp/secret');
        const allSecrets = await pppSecretMenu.get();

        client.close();

        res.json({
            cpuLoad: resource['cpu-load'] + '%',
            uptime: resource['uptime'],
            activePppoe: activePppoe.length.toString(),
            offlinePppoe: (allSecrets.length - activePppoe.length).toString()
        });
    } catch (error) {
        console.error("Mikrotik error:", error.message);
        res.status(500).json({ error: "Failed to connect to Mikrotik" });
    }
});
"""

# Append to file
content += mikrotik_endpoint

with open(filepath, 'w') as f:
    f.write(content)
