import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Using regex to find all instantiations of RouterOSClient and adding the error handler immediately after it
pattern = r"(const client = new RouterOSClient\(\{[\s\S]*?\}\);)"
replacement = r"\1\n        client.on('error', (err) => { console.error('RouterOS Client Error:', err.message || err); });"

content = re.sub(pattern, replacement, content)

with open("VPS/server.js", "w") as f:
    f.write(content)

