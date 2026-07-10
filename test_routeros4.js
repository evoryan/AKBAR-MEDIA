const { RouterOSClient } = require('routeros-client');
const client = new RouterOSClient({host: '1.2.3.4', user: 'admin', password: ''});
console.log(Object.getOwnPropertyNames(Object.getPrototypeOf(client.api)));
