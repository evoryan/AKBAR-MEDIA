import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Fix the res.status(500) to return a JSON object unconditionally
content = content.replace('res.status(500).json({ error: error.message || "Terjadi kesalahan" });', 'res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });')

with open('VPS/server.js', 'w') as f:
    f.write(content)
