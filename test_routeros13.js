const { RouterOSClient } = require('routeros-client');
async function test() {
    const api = new RouterOSClient({host: '1.2.3.4', user: 'admin', password: ''});
    try {
        const client = await api.connect();
        console.log(client.menu);
    } catch (e) {
        // Mock the connection to test
    }
}
test();
