import re

with open("VPS/server.js", "r") as f:
    content = f.read()

content = content.replace("AND status = 'BELUM BAYAR'", "")

with open("VPS/server.js", "w") as f:
    f.write(content)
