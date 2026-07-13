import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

content = content.replace("if (req.path === '/api/login') {", "if (req.path === '/api/login' || req.path === '/api/fix-db') {")

with open('VPS/server.js', 'w') as f:
    f.write(content)
