import re

filepath = 'VPS/server.js'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace("const PORT = process.env.PORT || 4500;", "const PORT = 4500;")

with open(filepath, 'w') as f:
    f.write(content)
