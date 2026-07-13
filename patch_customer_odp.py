import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# For POST /api/customers
old_cust_post = """        const [result] = await req.pool.query(
            'INSERT INTO customers (name, phone, area, username, billingDate, register_date, isolate_date, package_name, status, price, discount, additionalCost1, additionalCost2, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [name, phone, area, username, billingDate, registerDate || '', isolateDate || '', packageName || '', status, price, discount, additionalCost1 || 0, additionalCost2 || 0, pppoeSecret || '', odpId || '', odpPort || '']
        );"""

new_cust_post = """        const [result] = await req.pool.query(
            'INSERT INTO customers (name, phone, area, username, billingDate, register_date, isolate_date, package_name, status, price, discount, additionalCost1, additionalCost2, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            [name, phone, area, username, billingDate, registerDate || '', isolateDate || '', packageName || '', status, price, discount, additionalCost1 || 0, additionalCost2 || 0, pppoeSecret || '', (odpId && odpId !== '') ? parseInt(odpId) : null, odpPort || '']
        );"""

content = content.replace(old_cust_post, new_cust_post)

with open('VPS/server.js', 'w') as f:
    f.write(content)
