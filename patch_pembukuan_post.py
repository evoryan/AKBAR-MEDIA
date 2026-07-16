import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """app.post('/api/pembukuan', async (req, res) => {
    try {
        const { type, amount, description, category } = req.body;"""

rep = """app.post('/api/pembukuan', async (req, res) => {
    try {
        await req.pool.query("ALTER TABLE pembukuan MODIFY COLUMN type VARCHAR(50)").catch(e => {});
        const { type, amount, description, category } = req.body;"""

content = content.replace(target, rep)

with open("VPS/server.js", "w") as f:
    f.write(content)
