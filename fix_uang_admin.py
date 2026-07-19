with open('VPS/server.js', 'r') as f:
    content = f.read()

old_query = """SELECT admin_name as adminName, SUM(amount) as totalAmount, COUNT(*) as jmlPlggn 
            FROM pembukuan 
            WHERE type = 'pemasukan' AND admin_name IS NOT NULL
            GROUP BY admin_name"""

new_query = """SELECT p.admin_name as adminName, SUM(p.amount) as totalAmount, 
            (SELECT COUNT(*) FROM tagihan_bulanan t WHERE t.admin_name = p.admin_name AND t.status = 'LUNAS CASH') as jmlPlggn
            FROM pembukuan p
            WHERE p.type = 'pemasukan' AND p.admin_name IS NOT NULL
            GROUP BY p.admin_name"""

if old_query in content:
    content = content.replace(old_query, new_query)
    with open('VPS/server.js', 'w') as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("Query not found")
