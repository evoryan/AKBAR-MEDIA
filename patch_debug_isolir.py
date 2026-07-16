import re

with open("VPS/server.js", "r") as f:
    content = f.read()

old_code = """        const { id } = req.params;
        const [customers] = await req.pool.query('SELECT * FROM customers WHERE id = ?', [id]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customer = customers[0];"""

new_code = """        const { id } = req.params;
        console.log("ISOLIR REQUEST for id:", id);
        const [customers] = await req.pool.query('SELECT * FROM customers WHERE id = ?', [id]);
        if (customers.length === 0) {
            console.log("Customer not found in DB:", id);
            return res.status(404).json({ error: "Customer not found" });
        }
        const customer = customers[0];"""

content = content.replace(old_code, new_code)

old_mikrotik_code = """        let results = await client.rosApi.write('/ppp/secret/print', [`?.id=${identifier}`]);
        if (results.length > 0) {
            realId = results[0]['.id'] || results[0].id;"""

new_mikrotik_code = """        let results = await client.rosApi.write('/ppp/secret/print', [`?.id=${identifier}`]);
        console.log("Mikrotik print result for .id=", identifier, " is ", results);
        if (results.length > 0) {
            realId = results[0]['.id'] || results[0].id;"""

content = content.replace(old_mikrotik_code, new_mikrotik_code)

old_fallback = """            results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
            if (results.length > 0) {"""

new_fallback = """            results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
            console.log("Mikrotik print result for name=", identifier, " is ", results);
            if (results.length > 0) {"""

content = content.replace(old_fallback, new_fallback)

with open("VPS/server.js", "w") as f:
    f.write(content)

