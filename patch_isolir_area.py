import re

with open("VPS/server.js", "r") as f:
    content = f.read()

old_area_check = """        const [areas] = await req.pool.query('SELECT * FROM areas WHERE name = ?', [customer.area]);
        if (areas.length === 0) return res.status(404).json({ error: "Area not found for this customer" });
        const area = areas[0];"""

new_area_check = """        let [areas] = await req.pool.query('SELECT * FROM areas WHERE name = ?', [customer.area]);
        if (areas.length === 0) {
            // Fallback to first available area if not found, useful if area="Semua"
            const [allAreas] = await req.pool.query('SELECT * FROM areas LIMIT 1');
            if (allAreas.length > 0) {
                areas = allAreas;
            } else {
                return res.status(404).json({ error: "Area not found for this customer" });
            }
        }
        const area = areas[0];"""

content = content.replace(old_area_check, new_area_check)

with open("VPS/server.js", "w") as f:
    f.write(content)
