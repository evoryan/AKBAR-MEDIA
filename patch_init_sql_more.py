import re

with open('VPS/init.sql', 'r') as f:
    content = f.read()

new_columns = """    register_date VARCHAR(50) DEFAULT "",
    isolate_date VARCHAR(50) DEFAULT "",
    package_name VARCHAR(100) DEFAULT "",
    pppoe_secret VARCHAR(100) DEFAULT "",
    odp_id VARCHAR(50) DEFAULT "",
    odp_port VARCHAR(10) DEFAULT ""
"""

content = re.sub(
    r'register_date VARCHAR\(50\) DEFAULT "",\s*isolate_date VARCHAR\(50\) DEFAULT "",\s*package_name VARCHAR\(100\) DEFAULT ""',
    new_columns.strip(),
    content
)

with open('VPS/init.sql', 'w') as f:
    f.write(content)
