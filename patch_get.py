import re

with open("VPS/server.js", "r") as f:
    content = f.read()

content = content.replace("id: s['.id'],", "id: s['.id'] || s.id,")
content = content.replace("activeId = results[0]['.id'];", "activeId = results[0]['.id'] || results[0].id;")
content = content.replace("realId = results[0]['.id'];", "realId = results[0]['.id'] || results[0].id;")

with open("VPS/server.js", "w") as f:
    f.write(content)
