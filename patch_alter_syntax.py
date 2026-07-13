import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Replace masterPool.query multiple ADD COLUMN with individual ones
content = re.sub(r'await masterPool\.query\(`ALTER TABLE (.*?) ADD COLUMN (.*?), ADD COLUMN (.*?)`\)\.catch\(e => \{ /\*.*?\*/ \}\);',
                 r'await masterPool.query(`ALTER TABLE \1 ADD COLUMN \2`).catch(e=>{}); await masterPool.query(`ALTER TABLE \1 ADD COLUMN \3`).catch(e=>{});',
                 content)

# Replace tenantPools[dbName].query multiple ADD COLUMN with individual ones
content = re.sub(r'tenantPools\[dbName\]\.query\(`ALTER TABLE (.*?) ADD COLUMN (.*?), ADD COLUMN (.*?)`\)\.catch\(e => \{ /\*.*?\*/ \}\);',
                 r'tenantPools[dbName].query(`ALTER TABLE \1 ADD COLUMN \2`).catch(e=>{}); tenantPools[dbName].query(`ALTER TABLE \1 ADD COLUMN \3`).catch(e=>{});',
                 content)

with open('VPS/server.js', 'w') as f:
    f.write(content)
