# Update Server VPS untuk Mendukung Area pada Admin

Untuk mendukung fitur pemilihan **Area** pada Admin, Anda perlu melakukan sedikit penyesuaian pada file `server.js` dan database di VPS Anda.

### 1. Update Database (MySQL/MariaDB)
Jalankan query berikut pada database master Anda (`akbar_media_master`):

```sql
ALTER TABLE users ADD COLUMN area_id VARCHAR(100) DEFAULT 'semua';
```

### 2. Update `server.js`

Temukan endpoint `GET /api/admins` dan tambahkan `area_id` pada query SELECT:
```javascript
app.get('/api/admins', async (req, res) => {
    try {
        const db_name = req.user ? req.user.db_name : 'app_db';
        // UBAH BARIS INI:
        const [rows] = await masterPool.query('SELECT id, name, username, role, area_id FROM users WHERE db_name = ?', [db_name]);
        const admins = rows.map(r => ({ ...r, id: r.id.toString() }));
        res.json(admins);
    } catch (error) {
        // ...
    }
});
```

Temukan endpoint `POST /api/admins` (Tambah Admin) dan sesuaikan:
```javascript
app.post('/api/admins', async (req, res) => {
    try {
        // TAMBAHKAN area_id DISINI:
        const { name, username, role, password, area_id } = req.body;
        const db_name = req.user ? req.user.db_name : 'app_db';
        
        // UBAH QUERY INSERT INI:
        const [result] = await masterPool.query(
            'INSERT INTO users (name, username, role, password, db_name, area_id) VALUES (?, ?, ?, ?, ?, ?)',
            [name, username, role || 'ADMIN', password || '', db_name, area_id || 'semua']
        );
        res.json({ message: "Admin ditambahkan", id: result.insertId.toString() });
    } catch (error) {
        // ...
    }
});
```

Temukan endpoint `PUT /api/admins/:id` (Update Admin) dan sesuaikan:
```javascript
app.put('/api/admins/:id', async (req, res) => {
    try {
        // TAMBAHKAN area_id DISINI:
        const { name, username, role, password, area_id } = req.body;
        const db_name = req.user ? req.user.db_name : 'app_db';
        
        // ... (pengecekan username) ...

        // UBAH QUERY UPDATE INI:
        if (password) {
            await masterPool.query('UPDATE users SET name = ?, username = ?, role = ?, password = ?, area_id = ? WHERE id = ? AND db_name = ?', [name, username, role || 'ADMIN', password, area_id || 'semua', req.params.id, db_name]);
        } else {
            await masterPool.query('UPDATE users SET name = ?, username = ?, role = ?, area_id = ? WHERE id = ? AND db_name = ?', [name, username, role || 'ADMIN', area_id || 'semua', req.params.id, db_name]);
        }
        res.json({ message: "Admin diperbarui" });
    } catch (error) {
        // ...
    }
});
```
