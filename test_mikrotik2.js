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
    if(areas.length === 0) return;
    const area = areas[0];
    let host = area.routerIp;
    let port = 8728;
    if (host.includes(':')) {
        const parts = host.split(':');
        host = parts[0];
        port = parseInt(parts[1]);
    }

    const client = new RouterOSClient({
        host: host,
        user: area.mikrotikUser,
        password: area.mikrotikPassword,
        port: port,
        timeout: 5000
    });
    
    try {
        const api = await client.connect();
        const secretMenu = api.menu('/ppp/secret');
        const secrets = await secretMenu.get();
        console.log("Secret sample:", secrets[0]);
        client.close();
    } catch(e) { console.error(e); }
    conn.end();
}
run();
