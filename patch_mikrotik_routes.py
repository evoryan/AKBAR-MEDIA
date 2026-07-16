import re

with open("VPS/server.js", "r") as f:
    content = f.read()

def add_tenantContext(match):
    route = match.group(1)
    if "tenantContext" not in route:
        return f"{route}, tenantContext, async (req, res) => {{"
    return match.group(0)

# Replace all app.get and app.post for /api/mikrotik/
pattern = r"(app\.(get|post|delete)\('/api/mikrotik/.*?'\))\,?\s*async\s*\(\s*req\s*,\s*res\s*\)\s*=>\s*\{"
content = re.sub(pattern, r"\1, tenantContext, async (req, res) => {", content)

with open("VPS/server.js", "w") as f:
    f.write(content)

