import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Make the 404s inside isolir return 400 instead so we know it's not a missing route
content = content.replace('res.status(404).json({ error: "Customer not found" })', 'res.status(400).json({ error: "Customer tidak ditemukan di database" })')
content = content.replace('res.status(404).json({ error: "Area not found for this customer" })', 'res.status(400).json({ error: "Area router tidak ditemukan" })')
content = content.replace('res.status(404).json({ error: "Secret not found in Mikrotik" })', 'res.status(400).json({ error: "Secret tidak ditemukan di Mikrotik" })')

# ensure we catch mikrotik errors gracefully and return 500 with mikrotik message
old_catch = """        client.close();
        
        // Update customer status in database
        try {
            await req.pool.query('UPDATE customers SET status = ? WHERE id = ?', ['ISOLIR', id]);
        } catch (dbErr) {
            console.error("Error updating customer status:", dbErr);
        }

        res.json({ message: "Pelanggan berhasil di-isolir" });
    } catch (error) {
        console.error("Error isolating customer:", error);
        res.status(500).json({ error: "Gagal mengisolir pelanggan: " + error.message });
    }
});"""

new_catch = """        client.close();
        
        // Update customer status in database
        try {
            await req.pool.query('UPDATE customers SET status = ? WHERE id = ?', ['ISOLIR', id]);
        } catch (dbErr) {
            console.error("Error updating customer status:", dbErr);
        }

        res.json({ message: "Pelanggan berhasil di-isolir" });
    } catch (error) {
        console.error("Error isolating customer:", error);
        let msg = error.message;
        if (error.stack && error.stack.includes('routeros')) {
             msg = "Koneksi ke Mikrotik gagal atau timeout.";
        }
        res.status(500).json({ error: "Gagal mengisolir pelanggan: " + msg });
    }
});"""

content = content.replace(old_catch, new_catch)

with open("VPS/server.js", "w") as f:
    f.write(content)
