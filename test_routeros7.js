const { RouterOSClient } = require('routeros-client');
const client = new RouterOSClient({host: '1.2.3.4', user: 'admin', password: ''});
console.log(client.api);
console.log(typeof client.api);
console.log(Object.getOwnPropertyNames(client.api.constructor.prototype));
