import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """app.put('/api/pembukuan/:id', async (req, res) => {
    try {
        const { type, category, amount, description } = req.body;"""

rep = """app.put('/api/pembukuan/:id', async (req, res) => {
    try {
        await req.pool.query("ALTER TABLE pembukuan MODIFY COLUMN type VARCHAR(50)").catch(e => {});
        const { type, category, amount, description } = req.body;"""

content = content.replace(target, rep)

with open("VPS/server.js", "w") as f:
    f.write(content)
