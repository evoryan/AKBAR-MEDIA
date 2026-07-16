const mysql = require('mysql2/promise');
async function run() {
    const conn = await mysql.createConnection({
        host: '103.253.245.25',
        user: 'akbar',
        password: '08Delapan',
        database: 'akbar_media_master'
    });
    const [areas] = await conn.query('SELECT * FROM areas');
    console.log("Areas:", areas);
    conn.end();
}
run().catch(console.error);
