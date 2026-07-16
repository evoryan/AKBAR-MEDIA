const { RouterOSClient } = require('routeros-client');
async function run() {
    const client = new RouterOSClient({
        host: '103.253.245.25', // Wait, router IP is from the database. Let's fetch from the database.
    });
}
