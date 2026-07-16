import re

with open("VPS/server.js", "r") as f:
    content = f.read()

billing_pay_regex = r"(// Add notification.*?)(res\.json\(\{ message: \"Pembayaran berhasil dicatat\" \}\);)"
new_logic = """\\1
        // Automatis Enable Secret Mikrotik
        try {
            const [custData] = await req.pool.query('SELECT area, username, pppoe_secret FROM customers WHERE id = ?', [customerId]);
            if (custData.length > 0) {
                const customer = custData[0];
                const identifier = customer.pppoe_secret || customer.username;
                if (identifier && customer.area) {
                    const [areas] = await req.pool.query('SELECT * FROM areas WHERE name = ? OR id = ?', [customer.area, customer.area]);
                    if (areas.length > 0) {
                        const area = areas[0];
                        if (area.routerIp && area.mikrotikUser && area.mikrotikPassword) {
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
                                port: port,
                                timeout: 5000
                            });
                            client.on('error', (err) => { console.error('RouterOS Client Error in auto-enable:', err.message || err); });
                            
                            await client.connect();
                            
                            let realId = null;
                            let results = await client.rosApi.write('/ppp/secret/print', [`?name=${identifier}`]);
                            if (results.length > 0) realId = results[0]['.id'] || results[0].id;
                            
                            if (realId) {
                                try {
                                    await client.rosApi.write('/ppp/secret/enable', [
                                        `=.id=${realId}`
                                    ]);
                                    console.log(`Auto-enabled Mikrotik secret for ${identifier}`);
                                } catch (err) {
                                    if (err.message && err.message.includes('!empty')) {
                                        console.log(`Ignored !empty response from Mikrotik while auto-enabling ${identifier}`);
                                    } else {
                                        console.error("Failed to auto-enable secret:", err.message);
                                    }
                                }
                            }
                            
                            client.close();
                        }
                    }
                }
            }
        } catch (e) {
            console.error("Error auto-enabling Mikrotik secret:", e.message);
        }

        \\2"""

content = re.sub(billing_pay_regex, new_logic, content, flags=re.DOTALL)

with open("VPS/server.js", "w") as f:
    f.write(content)
