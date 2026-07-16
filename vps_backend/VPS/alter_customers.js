const mysql = require('mysql2/promise');
require('dotenv').config();

async function alterCustomers() {
    try {
        const connection = await mysql.createConnection({
            host: process.env.DB_HOST || 'localhost',
            user: process.env.DB_USER || 'akbar',
            password: process.env.DB_PASSWORD || '08Delapan',
            database: process.env.DB_NAME || 'app_db' // Assuming app_db for testing locally, but wait, it's multi-tenant now.
        });
        
        // Wait, since it's multi-tenant, we should alter ALL tenant databases. 
        // But for development here, maybe we just alter app_db?
        // Let's get all tenants from akbar_media_master
        await connection.query("USE akbar_media_master");
        const [users] = await connection.query("SELECT db_name FROM users WHERE role='SUPER_ADMIN'");
        
        for (const user of users) {
            const dbName = user.db_name;
            console.log(`Altering DB ${dbName}...`);
            await connection.query(`USE \`${dbName}\``);
            try { await connection.query(`ALTER TABLE customers ADD COLUMN register_date VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await connection.query(`ALTER TABLE customers ADD COLUMN isolate_date VARCHAR(50) DEFAULT ''`); } catch(e) {}
            try { await connection.query(`ALTER TABLE customers ADD COLUMN package_name VARCHAR(100) DEFAULT ''`); } catch(e) {}
        }
        await connection.end();
        console.log("Done");
    } catch (err) {
        console.error(err);
    }
}
alterCustomers();
