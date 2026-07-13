const mysql = require('mysql2/promise');
async function test() {
    try {
        const pool = mysql.createPool({
            host: '103.253.245.25',
            user: 'akbar',
            password: '08Delapan',
            database: 'akbar_media_master'
        });
        
        await pool.query(
            'INSERT INTO customers (name, phone, area, username, billingDate, status, price, discount, register_date, isolate_date, package_name, pppoe_secret, odp_id, odp_port) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
            ['Test', '08', 'Area', 'test', '1', 'Aktif', 'Rp 0', '0', '', '', '', '', '', '']
        );
        console.log("Success");
        process.exit(0);
    } catch(e) {
        console.error("DB Error:", e.message);
        process.exit(1);
    }
}
test();
