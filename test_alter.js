const mysql = require('mysql2/promise');
async function test() {
    try {
        console.log("Connecting...");
        const pool = mysql.createPool({
            host: '103.253.245.25',
            user: 'akbar',
            password: '08Delapan',
            database: 'akbar_media_master',
            connectTimeout: 5000
        });
        
        console.log("Altering ODC...");
        await pool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e => console.log("ODC portCount err:", e.message));
        await pool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e => console.log("ODC portInput err:", e.message));
        
        console.log("Altering ODP...");
        await pool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e => console.log("ODP portCount err:", e.message));
        await pool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e => console.log("ODP portInput err:", e.message));
        
        console.log("Done");
        process.exit(0);
    } catch(e) {
        console.error("Fatal:", e.message);
        process.exit(1);
    }
}
test();
