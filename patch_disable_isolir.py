import re

with open("VPS/server.js", "r") as f:
    content = f.read()

replacement = """        client.close();

        if (secretName) {
            try {
                // Update customer status to ISOLIR
                await req.pool.query(
                    "UPDATE customers SET status = 'ISOLIR' WHERE (username = ? OR pppoe_secret = ?) AND status = 'BELUM BAYAR'", 
                    [secretName, secretName]
                );
            } catch (dbErr) {
                console.error("Error updating customer status after disable:", dbErr);
            }
        }

        res.json({ message: "Secret berhasil di-disable dan active connection diremove (jika ada)" });"""

content = content.replace('        client.close();\n        res.json({ message: "Secret berhasil di-disable dan active connection diremove (jika ada)" });', replacement)

with open("VPS/server.js", "w") as f:
    f.write(content)
