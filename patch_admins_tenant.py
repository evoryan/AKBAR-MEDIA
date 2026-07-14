import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Fix /api/admins GET
target_get = """app.get('/api/admins', async (req, res) => {
    try {
        const [rows] = await masterPool.query('SELECT id, name, username, role FROM users');"""

rep_get = """app.get('/api/admins', async (req, res) => {
    try {
        const db_name = req.user ? req.user.db_name : 'app_db';
        const [rows] = await masterPool.query('SELECT id, name, username, role FROM users WHERE db_name = ?', [db_name]);"""
content = content.replace(target_get, rep_get)

# Fix /api/admins DELETE
target_delete = """app.delete('/api/admins/:id', async (req, res) => {
    try {
        await masterPool.query('DELETE FROM users WHERE id = ?', [req.params.id]);"""

rep_delete = """app.delete('/api/admins/:id', async (req, res) => {
    try {
        const db_name = req.user ? req.user.db_name : 'app_db';
        await masterPool.query('DELETE FROM users WHERE id = ? AND db_name = ?', [req.params.id, db_name]);"""
content = content.replace(target_delete, rep_delete)

# Fix /api/admins POST
target_post = """app.post('/api/admins', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const [result] = await masterPool.query(
            'INSERT INTO users (name, username, role, password) VALUES (?, ?, ?, ?)',
            [name, username, role, password]
        );"""

rep_post = """app.post('/api/admins', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const db_name = req.user ? req.user.db_name : 'app_db';
        const [result] = await masterPool.query(
            'INSERT INTO users (name, username, role, password, db_name) VALUES (?, ?, ?, ?, ?)',
            [name, username, role || 'ADMIN', password || '', db_name]
        );"""
content = content.replace(target_post, rep_post)

# Fix /api/admins PUT
target_put = """app.put('/api/admins/:id', async (req, res) => {
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
        }"""

rep_put = """app.put('/api/admins/:id', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const db_name = req.user ? req.user.db_name : 'app_db';
        // Check if username is already taken by someone else
        const [existing] = await masterPool.query('SELECT id FROM users WHERE username = ? AND id != ?', [username, req.params.id]);
        if (existing.length > 0) {
            return res.status(400).json({ error: "Username sudah digunakan" });
        }
        
        if (password) {
            await masterPool.query('UPDATE users SET name = ?, username = ?, role = ?, password = ? WHERE id = ? AND db_name = ?', [name, username, role || 'ADMIN', password, req.params.id, db_name]);
        } else {
            await masterPool.query('UPDATE users SET name = ?, username = ?, role = ? WHERE id = ? AND db_name = ?', [name, username, role || 'ADMIN', req.params.id, db_name]);
        }"""
content = content.replace(target_put, rep_put)

with open("VPS/server.js", "w") as f:
    f.write(content)

