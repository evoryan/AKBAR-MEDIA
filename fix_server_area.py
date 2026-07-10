filepath = 'VPS/server.js'
with open(filepath, 'r') as f:
    content = f.read()

target = """        const { name, description, routerIp, apiDomain, customerCount } = req.body;
        const [result] = await pool.query(
            'INSERT INTO areas (name, description, routerIp, apiDomain, customerCount) VALUES (?, ?, ?, ?, ?)',
            [name, description, routerIp, apiDomain, customerCount || 0]
        );"""

replacement = """        const { name, description, routerIp, apiDomain, customerCount, mikrotikUser, mikrotikPassword, acsUser, acsPassword } = req.body;
        const [result] = await pool.query(
            'INSERT INTO areas (name, description, routerIp, apiDomain, customerCount, mikrotikUser, mikrotikPassword, acsUser, acsPassword) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [name, description, routerIp, apiDomain, customerCount || 0, mikrotikUser || '', mikrotikPassword || '', acsUser || '', acsPassword || '']
        );"""

content = content.replace(target, replacement)

with open(filepath, 'w') as f:
    f.write(content)
