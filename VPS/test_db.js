const mysql = require('mysql2/promise');
const config = {
  host: process.env.DB_HOST || '103.253.245.25',
  user: process.env.DB_USER || 'akbar',
  password: process.env.DB_PASSWORD || 'akbar',
  database: process.env.DB_NAME || 'akbar_media_master'
};

async function test() {
  const pool = mysql.createPool(config);
  try {
    const [rows] = await pool.query('SELECT type, category, SUM(amount) as total FROM pembukuan GROUP BY type, category');
    console.log(rows);
    const [rows2] = await pool.query('SELECT category, amount FROM pengeluaran');
    console.log(rows2);
  } catch(e) {
    console.error(e);
  }
  process.exit();
}
test();
