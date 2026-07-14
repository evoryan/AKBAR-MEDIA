const axios = require('axios');

async function test() {
  try {
    const loginRes = await axios.post('http://103.253.245.25:4500/api/login', { username: 'akbar', password: 'password' });
    const token = loginRes.data.token;
    console.log("Token:", token);
    
    const res = await axios.get('http://103.253.245.25:4500/api/pembukuan', {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log("GET pembukuan:", res.data);
    
    const res2 = await axios.get('http://103.253.245.25:4500/api/pengeluaran', {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log("GET pengeluaran:", res2.data);
  } catch (e) {
    console.error(e.response ? e.response.data : e.message);
  }
}
test();
