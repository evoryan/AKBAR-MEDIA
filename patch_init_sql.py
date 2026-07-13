import re

with open('VPS/init.sql', 'r') as f:
    content = f.read()

# Keep INSERT INTO users, but remove others.
tables = [
    "customers", "pembukuan", "areas", "odc_list", "odp_list",
    "categories", "inventory", "stock_history", "packages"
]

for t in tables:
    pattern = r"INSERT INTO " + t + r" .*?;\n"
    content = re.sub(pattern, "", content, flags=re.DOTALL)
    
with open('VPS/init.sql', 'w') as f:
    f.write(content)
