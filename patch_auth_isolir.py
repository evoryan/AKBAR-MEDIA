import re

with open("VPS/server.js", "r") as f:
    content = f.read()

content = content.replace("app.post('/api/customers/:id/isolir', async (req, res) => {", "app.post('/api/customers/:id/isolir', tenantContext, async (req, res) => {")

with open("VPS/server.js", "w") as f:
    f.write(content)
