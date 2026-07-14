import re

with open("VPS/server.js", "r") as f:
    content = f.read()

put_api = """
app.put('/api/admins/:id', async (req, res) => {
    try {
        const { name, username, password } = req.body;
        // Check if username is already taken by someone else
        const [existing] = await masterPool.query('SELECT id FROM users WHERE username = ? AND id != ?', [username, req.params.id]);
        if (existing.length > 0) {
            return res.status(400).json({ error: "Username sudah digunakan" });
        }
        
        if (password) {
            await masterPool.query('UPDATE users SET name = ?, username = ?, password = ? WHERE id = ?', [name, username, password, req.params.id]);
        } else {
            await masterPool.query('UPDATE users SET name = ?, username = ? WHERE id = ?', [name, username, req.params.id]);
        }
        res.json({ message: "Admin diperbarui" });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});
"""

target = """app.post('/api/admins', async (req, res) => {"""

if target in content and "app.put('/api/admins/:id'" not in content:
    content = content.replace(target, put_api + target)
    with open("VPS/server.js", "w") as f:
        f.write(content)
    print("Added PUT /api/admins/:id")
else:
    print("Already exists or target not found")
