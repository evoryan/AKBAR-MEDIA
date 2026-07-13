import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

old_query = """        const [paidCustomers] = await req.pool.query('SELECT COUNT(*) as paid FROM customers WHERE status = "Aktif"');
        const [unpaidCustomers] = await req.pool.query('SELECT COUNT(*) as unpaid FROM customers WHERE status != "Aktif"');"""

new_query = """        const [paidCustomers] = await req.pool.query('SELECT COUNT(*) as paid FROM customers WHERE status = "LUNAS CASH"');
        const [unpaidCustomers] = await req.pool.query('SELECT COUNT(*) as unpaid FROM customers WHERE status != "LUNAS CASH"');"""

content = content.replace(old_query, new_query)

with open('VPS/server.js', 'w') as f:
    f.write(content)
