import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Update ODC POST
old_odc_post = """app.post('/api/odc', async (req, res) => {
    try {
        const { code, name, capacity, maxOdp, portCount, status, area, location } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO odc_list (code, name, capacity, maxOdp, portCount, status, area, location) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            [code, name, capacity, maxOdp, portCount, status, area, location]
        );"""

new_odc_post = """app.post('/api/odc', async (req, res) => {
    try {
        const { code, name, capacity, maxOdp, portCount, status, area, location, portInput } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO odc_list (name, location, portCount, portInput) VALUES (?, ?, ?, ?)',
            [name, location, portCount || 0, portInput || '']
        );"""

content = content.replace(old_odc_post, new_odc_post)

# Update ODP POST
old_odp_post = """app.post('/api/odp', async (req, res) => {
    try {
        const { odcId, code, name, portCount, status, location } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO odp_list (odcId, code, name, portCount, status, location) VALUES (?, ?, ?, ?, ?, ?)',
            [odcId, code, name, portCount, status, location]
        );"""

new_odp_post = """app.post('/api/odp', async (req, res) => {
    try {
        const { odcId, code, name, portCount, status, location, portInput } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO odp_list (odcId, name, portCount, portInput) VALUES (?, ?, ?, ?)',
            [odcId, name, portCount || 0, portInput || '']
        );"""

content = content.replace(old_odp_post, new_odp_post)

# Also update GET endpoints for ODC and ODP to select the new fields
# Wait, let's see what they currently select. They probably do `SELECT *`.
with open('VPS/server.js', 'w') as f:
    f.write(content)
