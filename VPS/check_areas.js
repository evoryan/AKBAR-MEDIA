const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    const conn = await mysql.createConnection({
        host: process.env.DB_HOST || '103.253.245.25',
        user: process.env.DB_USER || 'akbar',
        password: process.env.DB_PASSWORD || '08Delapan',
        database: 'akbar_media_master'
    });
    const [areas] = await conn.query('SELECT name FROM areas');
    console.log("Areas:", areas);
    conn.end();
}
run().catch(console.error);
