import re

with open("VPS/server.js", "r") as f:
    content = f.read()

old_res = """        client.close();
        res.json({ message: "Pelanggan berhasil di-isolir" });"""

new_res = """        client.close();
        
        // Update customer status in database
        try {
            await req.pool.query('UPDATE customers SET status = ? WHERE id = ?', ['ISOLIR', id]);
        } catch (dbErr) {
            console.error("Error updating customer status:", dbErr);
        }

        res.json({ message: "Pelanggan berhasil di-isolir" });"""

content = content.replace(old_res, new_res)

with open("VPS/server.js", "w") as f:
    f.write(content)
