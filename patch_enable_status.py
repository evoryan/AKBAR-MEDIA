import re

with open("VPS/server.js", "r") as f:
    content = f.read()

replacement = """        client.close();

        let secretName = null;
        if (results.length > 0) {
            secretName = results[0].name;
        }

        if (secretName) {
            try {
                const [custs] = await req.pool.query('SELECT id FROM customers WHERE username = ? OR pppoe_secret = ?', [secretName, secretName]);
                if (custs.length > 0) {
                    const custId = custs[0].id;
                    const [tagihan] = await req.pool.query('SELECT id FROM tagihan_bulanan WHERE customer_id = ? AND status = "BELUM BAYAR"', [custId]);
                    if (tagihan.length > 0) {
                        await req.pool.query("UPDATE customers SET status = 'BELUM BAYAR' WHERE id = ?", [custId]);
                    } else {
                        await req.pool.query("UPDATE customers SET status = 'LUNAS CASH' WHERE id = ?", [custId]);
                    }
                }
            } catch (dbErr) {
                console.error("Error updating customer status after enable:", dbErr);
            }
        }

        res.json({ message: "Secret berhasil di-enable" });"""

content = content.replace('        client.close();\n        res.json({ message: "Secret berhasil di-enable" });', replacement)

with open("VPS/server.js", "w") as f:
    f.write(content)

