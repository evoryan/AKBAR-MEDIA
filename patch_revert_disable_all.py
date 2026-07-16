import re

with open("VPS/server.js", "r") as f:
    content = f.read()

content = content.replace("await client.rosApi.write('/ppp/secret/set', [\n                `=.id=${realId}`,\n                `=disabled=yes`\n            ]);", "await client.rosApi.write('/ppp/secret/disable', [\n                `=.id=${realId}`\n            ]);")

content = content.replace("await client.rosApi.write('/ppp/secret/set', [\n            `=.id=${realId}`,\n            `=disabled=yes`\n        ]);", "await client.rosApi.write('/ppp/secret/disable', [\n            `=.id=${realId}`\n        ]);")

with open("VPS/server.js", "w") as f:
    f.write(content)

