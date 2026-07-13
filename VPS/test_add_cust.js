const axios = require('axios');
async function test() {
    try {
        const loginRes = await axios.post('http://103.253.245.25:4500/api/login', {
            username: 'superadmin',
            password: 'password'
        });
        const token = loginRes.data.token;
        console.log("Got token.");
        
        const res = await axios.post('http://103.253.245.25:4500/api/customers', {
            name: 'Test Customer',
            phone: '0812345678',
            area: 'Area 1',
            username: 'testcust',
            billingDate: '1',
            status: 'BELUM BAYAR',
            price: 'Rp. 150.000',
            discount: '- Dskn : Rp. 0',
            registerDate: '12-07-2026',
            isolateDate: '20-07-2026',
            packageName: 'Paket 1',
            additionalCost1: '0',
            additionalCost2: '0',
            pppoeSecret: 'secret123',
            odpId: '1',
            odpPort: '1'
        }, {
            headers: { Authorization: `Bearer ${token}` }
        });
        console.log("Success:", res.data);
    } catch(e) {
        console.error("Error:", e.response ? e.response.data : e.message);
    }
}
test();
