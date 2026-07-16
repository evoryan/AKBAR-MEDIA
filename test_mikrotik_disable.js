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
        if (secrets.length > 0) {
            const secretName = secrets[0].name;
            console.log("Found secret to test:", secretName);
            
            try {
                await secretMenu.where({ name: secretName }).update({ disabled: "yes" });
                console.log("Update disable success!");
            } catch(e) {
                console.error("Update disable failed:", e);
            }
            
            try {
                await secretMenu.where({ name: secretName }).update({ disabled: "no" });
                console.log("Update enable success!");
            } catch(e) {}
        } else {
            console.log("No secrets found");
        }
        api.close();
    } catch(e) { console.error("Global Error:", e); }
    conn.end();
}
run();
