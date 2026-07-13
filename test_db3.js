const mysql = require('mysql2/promise');
async function test() {
    try {
        const pool = mysql.createPool({
            host: '103.253.245.25',
            user: 'akbar',
            password: '08Delapan',
            database: 'akbar_media_master'
        });
        
        await pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e => console.log(e.message));
        
        const [odpColumns] = await pool.query('SHOW COLUMNS FROM odp_list');
        console.log("ODP Columns:", odpColumns.map(c => c.Field));
        
        process.exit(0);
    } catch(e) {
        console.error("DB connection error:", e);
        process.exit(1);
    }
}
test();
