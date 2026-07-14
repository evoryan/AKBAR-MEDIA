const mysql = require('mysql2/promise');
const config = {
  host: process.env.DB_HOST || '103.253.245.25',
  user: process.env.DB_USER || 'akbar',
  password: process.env.DB_PASSWORD || '08Delapan',
  database: 'akbar_media_master'
};
async function test() {
  const pool = mysql.createPool(config);
  try {
    const [cols] = await pool.query('SHOW COLUMNS FROM pembukuan');
    console.log("pembukuan cols:", cols.map(c => c.Field));
  } catch(e) {
    console.error(e);
  }
  process.exit();
}
test();
