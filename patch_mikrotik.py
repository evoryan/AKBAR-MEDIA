import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Replace disable route body
disable_pattern = re.compile(r"await client\.connect\(\);\s*// Use raw node-routeros client to execute write\s*await client\.rosApi\.write\('/ppp/secret/set', \[\s*`=\.id=\$\{secretId\}`,\s*`=disabled=yes`\s*\]\);\s*client\.close\(\);", re.DOTALL)
disable_repl = """const api = await client.connect();
        const pppSecretMenu = api.menu('/ppp/secret');
        await pppSecretMenu.update({ disabled: "yes" }, secretId);
        client.close();"""
content = disable_pattern.sub(disable_repl, content)

# Replace enable route body
enable_pattern = re.compile(r"await client\.connect\(\);\s*// Use raw node-routeros client to execute write\s*await client\.rosApi\.write\('/ppp/secret/set', \[\s*`=\.id=\$\{secretId\}`,\s*`=disabled=no`\s*\]\);\s*client\.close\(\);", re.DOTALL)
enable_repl = """const api = await client.connect();
        const pppSecretMenu = api.menu('/ppp/secret');
        await pppSecretMenu.update({ disabled: "no" }, secretId);
        client.close();"""
content = enable_pattern.sub(enable_repl, content)

# Replace remove active route body
remove_pattern = re.compile(r"await client\.connect\(\);\s*// Find the active connection by name\s*const activeRows = await client\.rosApi\.write\('/ppp/active/print', \[`\?name=\$\{secretName\}`\]\);\s*if \(activeRows\.length > 0\) \{\s*const activeId = activeRows\[0\]\['\.id'\];\s*await client\.rosApi\.write\('/ppp/active/remove', \[\s*`=\.id=\$\{activeId\}`\s*\]\);\s*client\.close\(\);\s*return res\.json\(\{ message: \"Active connection berhasil diremove\" \}\);\s*\} else \{\s*client\.close\(\);\s*return res\.status\(404\)\.json\(\{ error: \"Active connection tidak ditemukan\" \}\);\s*\}", re.DOTALL)
remove_repl = """const api = await client.connect();
        const pppActiveMenu = api.menu('/ppp/active');
        const activeRows = await pppActiveMenu.where({ name: secretName }).get();
        if (activeRows.length > 0) {
            await pppActiveMenu.where({ name: secretName }).remove();
            client.close();
            return res.json({ message: "Active connection berhasil diremove" });
        } else {
            client.close();
            return res.status(404).json({ error: "Active connection tidak ditemukan" });
        }"""
content = remove_pattern.sub(remove_repl, content)

with open("VPS/server.js", "w") as f:
    f.write(content)

