const mysql = require('mysql2/promise');
require('dotenv').config({ path: '.env' });
async function test() {
    const pool = mysql.createPool({
        host: process.env.DB_HOST || 'localhost',
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'app_db'
    });
    const [rows] = await pool.query('SELECT price, status FROM customers');
    console.log(rows);
    let totalGlobalRevenue = 0;
    rows.forEach(row => {
        if (row.price && (row.status == null || row.status != "TERHAPUS")) {
            const priceStr = String(row.price).replace(/\.0$/, '').replace(/[^0-9]/g, '');
            const priceVal = parseInt(priceStr);
            if (!isNaN(priceVal)) totalGlobalRevenue += priceVal;
        }
    });
    console.log("Total: ", totalGlobalRevenue);
    process.exit(0);
}
test();
