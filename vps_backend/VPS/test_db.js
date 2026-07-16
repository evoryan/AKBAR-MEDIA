const mysql = require('mysql2/promise');
async function run() {
    const conn = await mysql.createConnection({
        host: '103.253.245.25',
        user: 'akbar',
        password: '08Delapan',
        database: 'akbar_media_master'
    });
    const [areas] = await conn.query('SELECT name FROM areas');
    console.log("Areas:", areas);
    const [customers] = await conn.query('SELECT id, area FROM customers LIMIT 5');
    console.log("Customers:", customers);
    conn.end();
}
run().catch(console.error);
