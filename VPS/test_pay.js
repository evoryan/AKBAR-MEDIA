const jwt = require('jsonwebtoken');

const token = jwt.sign({ id: 1, db_name: 'smartnet_test' }, process.env.JWT_SECRET || 'super-secret-key-akbar'); 

async function test() {
    try {
        const res = await fetch('http://localhost:4500/api/billing/pay', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` 
            },
            body: JSON.stringify({
                customerId: "1",
                adminName: "Admin",
                totalAmount: 50000
            })
        });
        const data = await res.json();
        console.log(res.status, data);
    } catch (err) {
        console.error(err);
    }
}
test();
