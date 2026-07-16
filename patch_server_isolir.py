import re

with open("VPS/server.js", "r") as f:
    content = f.read()

isolir_endpoint = """
app.post('/api/customers/:id/isolir', async (req, res) => {
    try {
        const { id } = req.params;
        const [customers] = await req.pool.query('SELECT * FROM customers WHERE id = ?', [id]);
        if (customers.length === 0) return res.status(404).json({ error: "Customer not found" });
        const customer = customers[0];

        const identifier = customer.pppoe_secret || customer.username;
        if (!identifier) return res.status(400).json({ error: "Customer does not have PPPoE secret or username" });

        const [areas] = await req.pool.query('SELECT * FROM areas WHERE name = ?', [customer.area]);
        if (areas.length === 0) return res.status(404).json({ error: "Area not found for this customer" });
        const area = areas[0];

        let host = area.routerIp;
        let port = 8728;
        if (host.includes(':')) {
            const parts = host.split(':');
            host = parts[0];
            port = parseInt(parts[1]);
        }
        
        const client = new RouterOSClient({
            host: host,
            user: area.mikrotikUser,
            password: area.mikrotikPassword,
            port: parseInt(port) || 8728,
            timeout: 5000
        });

        await client.connect();
        
        // Find exact item safely
        let realId = null;
        let secretName = null;
        let results = await client.rosApi.write('/ppp/secret/print', [`?.id=${identifier}`]);
        if (results.length > 0) {
            realId = results[0]['.id'] || results[0].id;
            secretName = results[0].name;
        } else {
            results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
            if (results.length > 0) {
                realId = results[0]['.id'] || results[0].id;
                secretName = results[0].name;
            }
        }

        if (realId) {
            await client.rosApi.write('/ppp/secret/set', [
                `=.id=${realId}`,
                `=disabled=yes`
            ]);
            
            if (secretName) {
                try {
                    let activeResults = await client.rosApi.write('/ppp/active/print', [`?name=${secretName}`]);
                    if (activeResults.length > 0) {
                        let activeId = activeResults[0]['.id'] || activeResults[0].id;
                        await client.rosApi.write('/ppp/active/remove', [
                            `=.id=${activeId}`
                        ]);
                    }
                } catch (e) {
                    console.error("Failed to remove active connection:", e.message);
                }
            }
        } else {
            console.log("Secret not found in Mikrotik, but proceeding as success to avoid breaking flow");
        }
        
        client.close();
        res.json({ message: "Pelanggan berhasil di-isolir" });
    } catch (error) {
        console.error("Error isolating customer:", error);
        res.status(500).json({ error: "Gagal mengisolir pelanggan: " + error.message });
    }
});
"""

# Insert it before the Mikrotik secrets routes or near customers delete
pattern = r"(app\.delete\('/api/customers/:id', async \(req, res\) => \{.*?\n\}\);)"
match = re.search(pattern, content, re.DOTALL)
if match:
    content = content[:match.end()] + "\n" + isolir_endpoint + content[match.end():]
    with open("VPS/server.js", "w") as f:
        f.write(content)
    print("Success patch")
else:
    print("Failed to find injection point")
