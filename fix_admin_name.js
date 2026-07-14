const fs = require('fs');
let code = fs.readFileSync('VPS/server.js', 'utf8');

code = code.replace(
  'await masterPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT \\\'Lain-lain\\\'`).catch(e=>{});',
  'await masterPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT \\\'Lain-lain\\\'`).catch(e=>{});\\n        await masterPool.query(`ALTER TABLE pembukuan ADD COLUMN admin_name VARCHAR(100) DEFAULT \\\'\\\'`).catch(e=>{});'
);

code = code.replace(
  'await tPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT \\\'Lain-lain\\\'`).catch(e=>{});',
  'await tPool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT \\\'Lain-lain\\\'`).catch(e=>{});\\n            await tPool.query(`ALTER TABLE pembukuan ADD COLUMN admin_name VARCHAR(100) DEFAULT \\\'\\\'`).catch(e=>{});'
);

code = code.replace(
  'try { await pool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT \\\'Lain-lain\\\'`); } catch(e) {}',
  'try { await pool.query(`ALTER TABLE pembukuan ADD COLUMN category VARCHAR(100) DEFAULT \\\'Lain-lain\\\'`); } catch(e) {}\\n            try { await pool.query(`ALTER TABLE pembukuan ADD COLUMN admin_name VARCHAR(100) DEFAULT \\\'\\\'`); } catch(e) {}'
);

fs.writeFileSync('VPS/server.js', code);
