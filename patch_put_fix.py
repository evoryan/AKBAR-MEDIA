import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

content = re.sub(
    r"app\.put\('/api/odc/:id', async \(req, res\) => \{\s*try \{\s*const \{.*?\} = req\.params;\s*const \{.*?\} = req\.body;\s*await req\.pool\.query\(\s*'UPDATE odc_list.*?WHERE id = \?',\s*\[(.*?)\]\s*\);",
    r"""app.put('/api/odc/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, location, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        await req.pool.query(
            'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',
            [name, location, parsedPortCount, portInput || '', id]
        );""",
    content,
    flags=re.DOTALL
)

content = re.sub(
    r"app\.put\('/api/odp/:id', async \(req, res\) => \{\s*try \{\s*const \{.*?\} = req\.params;\s*const \{.*?\} = req\.body;\s*await req\.pool\.query\(\s*'UPDATE odp_list.*?WHERE id = \?',\s*\[(.*?)\]\s*\);",
    r"""app.put('/api/odp/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, odcId, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        const parsedOdcId = parseInt(odcId) || 0;
        await req.pool.query(
            'UPDATE odp_list SET name = ?, odcId = ?, portCount = ?, portInput = ? WHERE id = ?',
            [name, parsedOdcId, parsedPortCount, portInput || '', id]
        );""",
    content,
    flags=re.DOTALL
)

with open('VPS/server.js', 'w') as f:
    f.write(content)
