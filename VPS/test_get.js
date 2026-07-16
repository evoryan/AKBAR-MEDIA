const { RouterOSClient } = require('routeros-client');
const mysql = require('mysql2/promise');
require('dotenv').config();

async function run() {
    const conn = await mysql.createConnection({
        host: process.env.DB_HOST || '103.253.245.25',
        user: process.env.DB_USER || 'akbar',
        password: process.env.DB_PASSWORD || '08Delapan',
        database: 'akbar_media_master'
    });
    const [areas] = await conn.query('SELECT * FROM areas LIMIT 1');
    const area = areas[0];
    const client = new RouterOSClient({
        host: area.routerIp.split(':')[0],
        user: area.mikrotikUser,
        password: area.mikrotikPassword,
        port: 8728,
        timeout: 5000
    });
    const api = await client.connect();
    const pppSecretMenu = api.menu('/ppp/secret');
    const allSecrets = await pppSecretMenu.get();
    console.log(allSecrets[0]);
    client.close();
    conn.end();
}
run().catch(console.error);
