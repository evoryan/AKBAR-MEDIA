import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

ping = "app.get('/api/ping', (req, res) => res.json({ status: 'ok' }));\n"
if "app.get('/api/ping'" not in content:
    content = content.replace("app.use(express.json());", "app.use(express.json());\n" + ping)

with open('VPS/server.js', 'w') as f:
    f.write(content)
