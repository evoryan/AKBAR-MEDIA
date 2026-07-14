const mysql = require('mysql2/promise');
const config = {
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'akbar_media_master'
};

async function test() {
  const pool = mysql.createPool(config);
  try {
    const [rows] = await pool.query('SELECT type, category, SUM(amount) as total FROM pembukuan GROUP BY type, category');
    console.log(rows);
  } catch(e) {
    console.error(e);
  }
  process.exit();
}
test();
