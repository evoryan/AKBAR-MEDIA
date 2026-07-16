import re

with open("VPS/server.js", "r") as f:
    content = f.read()

old_set = """        if (realId) {
            await client.rosApi.write('/ppp/secret/set', [
                `=.id=${realId}`,
                `=disabled=yes`
            ]);"""

new_set = """        if (realId) {
            await client.rosApi.write('/ppp/secret/disable', [
                `=.id=${realId}`
            ]);"""

content = content.replace(old_set, new_set)

# Also fix the other place if it exists
old_disable = """        await client.rosApi.write('/ppp/secret/set', [
            `=.id=${realId}`,
            `=disabled=yes`
        ]);"""

new_disable = """        await client.rosApi.write('/ppp/secret/disable', [
            `=.id=${realId}`
        ]);"""

content = content.replace(old_disable, new_disable)

old_enable = """        await client.rosApi.write('/ppp/secret/set', [
            `=.id=${realId}`,
            `=disabled=no`
        ]);"""
new_enable = """        await client.rosApi.write('/ppp/secret/enable', [
            `=.id=${realId}`
        ]);"""
content = content.replace(old_enable, new_enable)

with open("VPS/server.js", "w") as f:
    f.write(content)
