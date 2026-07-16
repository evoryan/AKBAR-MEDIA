import re

with open("VPS/init.sql", "r") as f:
    content = f.read()

# Remove odc_list and odp_list blocks and store them
odc_pattern = re.compile(r'CREATE TABLE IF NOT EXISTS odc_list.*?;', re.DOTALL)
odp_pattern = re.compile(r'CREATE TABLE IF NOT EXISTS odp_list.*?;', re.DOTALL)

odc_match = odc_pattern.search(content)
odp_match = odp_pattern.search(content)

content = content.replace(odc_match.group(0), "")
content = content.replace(odp_match.group(0), "")

# insert them before CREATE TABLE IF NOT EXISTS customers
insert_pos = content.find("CREATE TABLE IF NOT EXISTS customers")
new_content = content[:insert_pos] + odc_match.group(0) + odp_match.group(0) + content[insert_pos:]

with open("VPS/init.sql", "w") as f:
    f.write(new_content)

