import re

with open("VPS/server.js", "r") as f:
    content = f.read()

content = content.replace(
    "'SELECT price FROM customers WHERE status != \"TERHAPUS\"'",
    "'SELECT price FROM customers WHERE status IS NULL OR status != \"TERHAPUS\"'"
)

with open("VPS/server.js", "w") as f:
    f.write(content)
