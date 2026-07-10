const { RouterOSClient } = require('routeros-client');
const client = new RouterOSClient({host: '1.2.3.4', user: 'admin', password: ''});
const menu = client.menu('/system/resource');
console.log(Object.keys(menu.constructor.prototype));
