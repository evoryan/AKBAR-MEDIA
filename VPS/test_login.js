const axios = require('axios');
async function test() {
    try {
        const loginRes = await axios.post('http://103.253.245.25:4500/api/login', {
            username: 'superadmin',
            password: 'password'
        });
        const token = loginRes.data.token;
        console.log("Got token.");
        
        const res = await axios.post('http://103.253.245.25:4500/api/odc', {
            name: 'Test ODC',
            location: 'Loc',
            portCount: 8,
            portInput: '8'
        }, {
            headers: { Authorization: `Bearer ${token}` }
        });
        console.log("Success:", res.data);
    } catch(e) {
        console.error("Error:", e.response ? e.response.data : e.message);
    }
}
test();
