import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Fix /api/admins GET
target_get = """app.get('/api/admins', async (req, res) => {
    try {
        const [rows] = await req.pool.query('SELECT id, name, username, role FROM users');"""

rep_get = """app.get('/api/admins', async (req, res) => {
    try {
        const [rows] = await masterPool.query('SELECT id, name, username, role FROM users');"""
content = content.replace(target_get, rep_get)

# Fix /api/admins DELETE
target_delete = """app.delete('/api/admins/:id', async (req, res) => {
    try {
        await req.pool.query('DELETE FROM users WHERE id = ?', [req.params.id]);"""

rep_delete = """app.delete('/api/admins/:id', async (req, res) => {
    try {
        await masterPool.query('DELETE FROM users WHERE id = ?', [req.params.id]);"""
content = content.replace(target_delete, rep_delete)

# Fix /api/admins POST
target_post = """app.post('/api/admins', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO users (name, username, role, password) VALUES (?, ?, ?, ?)',
            [name, username, role, password]
        );"""

rep_post = """app.post('/api/admins', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const [result] = await masterPool.query(
            'INSERT INTO users (name, username, role, password) VALUES (?, ?, ?, ?)',
            [name, username, role, password]
        );"""
content = content.replace(target_post, rep_post)


# Fix FCM superadmin
target_fcm = """            if (req.user && req.user.db_name) {
                sendTenantNotification(req.user.db_name, "Pembayaran Diterima", notifMsg);
            }"""

rep_fcm = """            if (req.user && req.user.db_name) {
                sendTenantNotification(req.user.db_name, "Pembayaran Diterima", notifMsg);
            }
            sendTenantNotification("superadmin", "Pembayaran Diterima", notifMsg);"""
content = content.replace(target_fcm, rep_fcm)

with open("VPS/server.js", "w") as f:
    f.write(content)
