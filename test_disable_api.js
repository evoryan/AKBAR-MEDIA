const axios = require('axios');
async function test() {
    try {
        // Find area 1
        const res = await axios.post('http://localhost:4500/api/mikrotik/secrets/1/disable', { secretId: '*1' });
        console.log("Success:", res.data);
    } catch(e) {
        console.error("Error:", e.response ? e.response.data : e.message);
    }
}
test();
