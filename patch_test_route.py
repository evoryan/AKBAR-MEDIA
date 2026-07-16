import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """app.get('/api/pembukuan/all', async (req, res) => {"""

rep = """app.get('/api/pembukuan/test_all', async (req, res) => {
    try {
        const [rows] = await masterPool.query('SELECT * FROM pembukuan ORDER BY id DESC');
        res.json(rows);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Terjadi kesalahan server" });
    }
});

app.get('/api/pembukuan/all', async (req, res) => {"""

content = content.replace(target, rep)

with open("VPS/server.js", "w") as f:
    f.write(content)
