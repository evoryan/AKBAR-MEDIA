const mysql = require('mysql2/promise');
async function test() {
    try {
        const pool = mysql.createPool({
            host: 'localhost', user: 'akbar', password: '08Delapan', database: 'smartnet_test'
        });
        const [c] = await pool.query('SELECT name FROM customers LIMIT 1');
        console.log(c);
        process.exit(0);
    } catch(e) { console.error(e); process.exit(1); }
}
test();
