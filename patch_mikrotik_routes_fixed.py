import re

with open("VPS/server.js", "r") as f:
    content = f.read()

replacements = [
    ("app.get('/api/mikrotik/status/:id', async (req, res) => {", "app.get('/api/mikrotik/status/:id', tenantContext, async (req, res) => {"),
    ("app.post('/api/mikrotik/secrets/:id', async (req, res) => {", "app.post('/api/mikrotik/secrets/:id', tenantContext, async (req, res) => {"),
    ("app.get('/api/mikrotik/profiles/:id', async (req, res) => {", "app.get('/api/mikrotik/profiles/:id', tenantContext, async (req, res) => {"),
    ("app.post('/api/mikrotik/secrets/:id/disable', async (req, res) => {", "app.post('/api/mikrotik/secrets/:id/disable', tenantContext, async (req, res) => {"),
    ("app.post('/api/mikrotik/secrets/:id/enable', async (req, res) => {", "app.post('/api/mikrotik/secrets/:id/enable', tenantContext, async (req, res) => {"),
    ("app.post('/api/mikrotik/secrets/:id/remove-active', async (req, res) => {", "app.post('/api/mikrotik/secrets/:id/remove-active', tenantContext, async (req, res) => {"),
    ("app.get('/api/mikrotik/secrets/:id', async (req, res) => {", "app.get('/api/mikrotik/secrets/:id', tenantContext, async (req, res) => {")
]

for old, new in replacements:
    content = content.replace(old, new)

with open("VPS/server.js", "w") as f:
    f.write(content)

