const { RouterOSClient } = require('routeros-client');
const client = new RouterOSClient({host: '1.2.3.4', user: 'admin', password: ''});
const menu = client.api('/system/resource');
console.log(menu);
console.log(typeof menu);
console.log(Object.getOwnPropertyNames(menu.constructor.prototype));
