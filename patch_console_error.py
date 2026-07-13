import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

content = content.replace('res.status(500).json({ error: error.message || "Terjadi kesalahan" });', 'console.error("API Error:", error.message); res.status(500).json({ error: error.message || "Terjadi kesalahan" });')

with open('VPS/server.js', 'w') as f:
    f.write(content)
