import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# PATCH GET /api/admins
get_pattern = r"""app\.get\('/api/admins', async \(req, res\) => \{
    try \{
        const db_name = req\.user \? req\.user\.db_name : 'app_db';
        const \[rows\] = await masterPool\.query\('SELECT id, name, username, role FROM users WHERE db_name = \?', \[db_name\]\);"""
get_replacement = """app.get('/api/admins', async (req, res) => {
    try {
        const db_name = req.user ? req.user.db_name : 'app_db';
        // Try to get from tenant DB first, fallback to masterPool
        let rows = [];
        try {
            const [tenantRows] = await req.pool.query('SELECT id, name, username, role FROM users');
            rows = tenantRows;
        } catch (e) {
            const [masterRows] = await masterPool.query('SELECT id, name, username, role FROM users WHERE db_name = ?', [db_name]);
            rows = masterRows;
        }"""
content = re.sub(get_pattern, get_replacement, content)

# PATCH DELETE /api/admins
del_pattern = r"""app\.delete\('/api/admins/:id', async \(req, res\) => \{
    try \{
        const db_name = req\.user \? req\.user\.db_name : 'app_db';
        await masterPool\.query\('DELETE FROM users WHERE id = \? AND db_name = \?', \[req\.params\.id, db_name\]\);
        res\.json\(\{ message: "Admin berhasil dihapus" \}\);"""
del_replacement = """app.delete('/api/admins/:id', async (req, res) => {
    try {
        const db_name = req.user ? req.user.db_name : 'app_db';
        // Delete from master DB (using id)
        const [rows] = await masterPool.query('SELECT username FROM users WHERE id = ? AND db_name = ?', [req.params.id, db_name]);
        const username = rows.length > 0 ? rows[0].username : null;
        
        await masterPool.query('DELETE FROM users WHERE id = ? AND db_name = ?', [req.params.id, db_name]);
        
        // Also delete from tenant DB
        try {
            if (req.pool && req.pool !== masterPool && username) {
                await req.pool.query('DELETE FROM users WHERE username = ?', [username]);
            }
        } catch (e) { console.error("Error deleting from tenant DB:", e.message); }
        
        res.json({ message: "Admin berhasil dihapus" });"""
content = re.sub(del_pattern, del_replacement, content)

# PATCH PUT /api/admins
put_pattern = r"""app\.put\('/api/admins/:id', async \(req, res\) => \{
    try \{
        const \{ name, username, role, password \} = req\.body;
        const db_name = req\.user \? req\.user\.db_name : 'app_db';
        // Check if username is already taken by someone else
        const \[existing\] = await masterPool\.query\('SELECT id FROM users WHERE username = \? AND id != \?', \[username, req\.params\.id\]\);
        if \(existing\.length > 0\) \{
            return res\.status\(400\)\.json\(\{ error: "Username sudah digunakan" \}\);
        \}
        
        if \(password\) \{
            await masterPool\.query\('UPDATE users SET name = \?, username = \?, role = \?, password = \? WHERE id = \? AND db_name = \?', \[name, username, role \|\| 'ADMIN', password, req\.params\.id, db_name\]\);
        \} else \{
            await masterPool\.query\('UPDATE users SET name = \?, username = \?, role = \? WHERE id = \? AND db_name = \?', \[name, username, role \|\| 'ADMIN', req\.params\.id, db_name\]\);
        \}
        res\.json\(\{ message: "Admin diperbarui" \}\);"""
put_replacement = """app.put('/api/admins/:id', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const db_name = req.user ? req.user.db_name : 'app_db';
        // Get old username from master to update tenant DB properly
        const [oldUserRows] = await masterPool.query('SELECT username FROM users WHERE id = ? AND db_name = ?', [req.params.id, db_name]);
        const oldUsername = oldUserRows.length > 0 ? oldUserRows[0].username : null;
        
        // Check if username is already taken by someone else
        const [existing] = await masterPool.query('SELECT id FROM users WHERE username = ? AND id != ?', [username, req.params.id]);
        if (existing.length > 0) {
            return res.status(400).json({ error: "Username sudah digunakan" });
        }
        
        if (password) {
            await masterPool.query('UPDATE users SET name = ?, username = ?, role = ?, password = ? WHERE id = ? AND db_name = ?', [name, username, role || 'ADMIN', password, req.params.id, db_name]);
            try {
                if (req.pool && req.pool !== masterPool && oldUsername) {
                    await req.pool.query('UPDATE users SET name = ?, username = ?, role = ?, password = ? WHERE username = ?', [name, username, role || 'ADMIN', password, oldUsername]);
                }
            } catch (e) { console.error("Error updating tenant DB:", e.message); }
        } else {
            await masterPool.query('UPDATE users SET name = ?, username = ?, role = ? WHERE id = ? AND db_name = ?', [name, username, role || 'ADMIN', req.params.id, db_name]);
            try {
                if (req.pool && req.pool !== masterPool && oldUsername) {
                    await req.pool.query('UPDATE users SET name = ?, username = ?, role = ? WHERE username = ?', [name, username, role || 'ADMIN', oldUsername]);
                }
            } catch (e) { console.error("Error updating tenant DB:", e.message); }
        }
        res.json({ message: "Admin diperbarui" });"""
content = re.sub(put_pattern, put_replacement, content)

# PATCH POST /api/admins
post_pattern = r"""app\.post\('/api/admins', async \(req, res\) => \{
    try \{
        const \{ name, username, role, password \} = req\.body;
        const db_name = req\.user \? req\.user\.db_name : 'app_db';
        const \[result\] = await masterPool\.query\(
            'INSERT INTO users \(name, username, role, password, db_name\) VALUES \(\?, \?, \?, \?, \?\)',
            \[name, username, role \|\| 'ADMIN', password \|\| '', db_name\]
        \);
        res\.json\(\{ message: "Admin ditambahkan", id: result\.insertId\.toString\(\) \}\);"""
post_replacement = """app.post('/api/admins', async (req, res) => {
    try {
        const { name, username, role, password } = req.body;
        const db_name = req.user ? req.user.db_name : 'app_db';
        const [result] = await masterPool.query(
            'INSERT INTO users (name, username, role, password, db_name) VALUES (?, ?, ?, ?, ?)',
            [name, username, role || 'ADMIN', password || '', db_name]
        );
        
        // Also insert into tenant DB
        try {
            if (req.pool && req.pool !== masterPool) {
                // Check if table exists
                await req.pool.query(
                    'INSERT INTO users (name, username, role, password) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=?, role=?, password=?',
                    [name, username, role || 'ADMIN', password || '', name, role || 'ADMIN', password || '']
                );
            }
        } catch (e) { console.error("Error inserting to tenant DB:", e.message); }
        
        res.json({ message: "Admin ditambahkan", id: result.insertId.toString() });"""
content = re.sub(post_pattern, post_replacement, content)

with open("VPS/server.js", "w") as f:
    f.write(content)
