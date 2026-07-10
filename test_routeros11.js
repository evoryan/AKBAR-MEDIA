const { RouterOSClient } = require('routeros-client');
async function test() {
    const client = new RouterOSClient({host: '1.2.3.4', user: 'admin', password: ''});
    try {
        const conn = await client.connect();
    } catch(e) {
        console.log("Error:", e.message);
    }
}
test();
