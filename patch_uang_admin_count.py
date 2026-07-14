import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """            SELECT admin_name as adminName, SUM(amount) as totalAmount 
            FROM pembukuan 
            WHERE category = 'Transaksi Cash' AND type = 'pemasukan' 
            GROUP BY admin_name"""

rep = """            SELECT admin_name as adminName, SUM(amount) as totalAmount, COUNT(*) as jmlPlggn 
            FROM pembukuan 
            WHERE category = 'Transaksi Cash' AND type = 'pemasukan' 
            GROUP BY admin_name"""

content = content.replace(target, rep)

with open("VPS/server.js", "w") as f:
    f.write(content)
