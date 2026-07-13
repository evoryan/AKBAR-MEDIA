import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

old_post = """app.post('/api/customers', async (req, res) => {
    try {
        const { name, phone, area, username, billingDate, status, price, discount, registerDate, isolateDate, packageName, additionalCost1, additionalCost2, pppoeSecret, odpId, odpPort } = req.body;
        
        const [result] = await req.pool.query(
            'INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [name, phone, area, username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', odpId || '', odpPort || '']
        );"""

new_post = """app.post('/api/customers', async (req, res) => {
    try {
        const { name, phone, area, username, billingDate, status, price, discount, registerDate, isolateDate, packageName, additionalCost1, additionalCost2, pppoeSecret, odpId, odpPort } = req.body;
        
        const parsedOdpId = (odpId !== undefined && odpId !== null && odpId !== '') ? parseInt(odpId) : null;
        
        const [result] = await req.pool.query(
            'INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [name, phone, area, username, billingDate, status, price, discount, registerDate || '', isolateDate || '', packageName || '', pppoeSecret || '', parsedOdpId, odpPort || '']
        );"""

content = content.replace(old_post, new_post)

with open('VPS/server.js', 'w') as f:
    f.write(content)
