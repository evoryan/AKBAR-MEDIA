import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Let's target it more loosely using regex since exact string matching failed
import re

# ODP POST
content = re.sub(
    r"app\.post\('/api/odp', async \(req, res\) => \{\s*try \{\s*const \{.*?\} = req\.body;\s*const \[result\] = await req\.pool\.query\(\s*'INSERT INTO odp_list.*?VALUES.*?',\s*\[(.*?)\]\s*\);",
    r"""app.post('/api/odp', async (req, res) => {
    try {
        const { odcId, name, portCount, portInput } = req.body;
        const parsedOdcId = parseInt(odcId) || 0;
        const parsedPortCount = parseInt(portCount) || 0;
        const [result] = await req.pool.query(
            'INSERT INTO odp_list (name, odcId, portCount, portInput) VALUES (?, ?, ?, ?)',
            [name, parsedOdcId, parsedPortCount, portInput || '']
        );""",
    content,
    flags=re.DOTALL
)

with open('VPS/server.js', 'w') as f:
    f.write(content)

