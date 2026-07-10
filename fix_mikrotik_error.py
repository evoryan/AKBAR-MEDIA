filepath = 'VPS/server.js'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('res.status(500).json({ error: "Failed to connect to Mikrotik" });', 'res.status(500).json({ error: "Failed to connect to Mikrotik: " + error.message });')

with open(filepath, 'w') as f:
    f.write(content)
