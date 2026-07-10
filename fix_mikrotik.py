import re

filepath = 'VPS/server.js'
with open(filepath, 'r') as f:
    content = f.read()

target = """        const client = new RouterOSClient({
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

        client.close();"""

replacement = """        const api = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        const client = await api.connect();

        const resourceMenu = client.menu('/system/resource');
        const resources = await resourceMenu.get();
        const resource = resources[0];

        const pppoeActiveMenu = client.menu('/ppp/active');
        const activePppoe = await pppoeActiveMenu.get();
        
        const pppSecretMenu = client.menu('/ppp/secret');
        const allSecrets = await pppSecretMenu.get();

        client.close();"""

content = content.replace(target, replacement)

with open(filepath, 'w') as f:
    f.write(content)
