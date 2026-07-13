import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

content = content.replace('res.status(500).json({ error: "Terjadi kesalahan" });', 'res.status(500).json({ error: error.message || "Terjadi kesalahan" });')

with open('VPS/server.js', 'w') as f:
    f.write(content)
