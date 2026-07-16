import re

with open("VPS/server.js", "r") as f:
    content = f.read()

old_disable = """            await client.rosApi.write('/ppp/secret/disable', [
                `=.id=${realId}`
            ]);"""

new_disable = """            await client.rosApi.write('/ppp/secret/set', [
                `=.id=${realId}`,
                `=disabled=yes`
            ]);"""

content = content.replace(old_disable, new_disable)

old_enable = """        await client.rosApi.write('/ppp/secret/enable', [
            `=.id=${realId}`
        ]);"""

new_enable = """        await client.rosApi.write('/ppp/secret/set', [
            `=.id=${realId}`,
            `=disabled=no`
        ]);"""

content = content.replace(old_enable, new_enable)

with open("VPS/server.js", "w") as f:
    f.write(content)
