const mysql = require('mysql2/promise');
async function test() {
    try {
        const pool = mysql.createPool({ host: '127.0.0.1', user: 'root', database: 'test' });
        await pool.query('SELECT * FROM test WHERE id = ?', [undefined]);
        console.log("query ok");
    } catch(e) { console.error("Error querying:", e.message); }
    process.exit(0);
}
test();
