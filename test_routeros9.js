const { RouterOSClient } = require('routeros-client');
const client = new RouterOSClient({host: '1.2.3.4', user: 'admin', password: ''});
const menu = client.api('/system/resource');
const model = menu.model();
console.log(model);
console.log(Object.getOwnPropertyNames(model.constructor.prototype));
