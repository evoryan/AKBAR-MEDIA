import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Make sure ODC POST route parses portCount as an integer and uses the correct fields
old_odc_post = """app.post('/api/odc', async (req, res) => {
    try {
        const { code, name, capacity, maxOdp, portCount, status, area, location, portInput } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO odc_list (name, location, portCount, portInput) VALUES (?, ?, ?, ?)',
            [name, location, portCount || 0, portInput || '']
        );"""

new_odc_post = """app.post('/api/odc', async (req, res) => {
    try {
        const { name, location, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        const [result] = await req.pool.query(
            'INSERT INTO odc_list (name, location, portCount, portInput) VALUES (?, ?, ?, ?)',
            [name, location, parsedPortCount, portInput || '']
        );"""

content = content.replace(old_odc_post, new_odc_post)

old_odp_post = """app.post('/api/odp', async (req, res) => {
    try {
        const { name, odcId, portCount } = req.body;
        const [result] = await req.pool.query(
            'INSERT INTO odp_list (name, odcId, portCount) VALUES (?, ?, ?)',
            [name, odcId, portCount || 0]
        );"""

new_odp_post = """app.post('/api/odp', async (req, res) => {
    try {
        const { name, odcId, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        const parsedOdcId = parseInt(odcId) || 0;
        const [result] = await req.pool.query(
            'INSERT INTO odp_list (name, odcId, portCount, portInput) VALUES (?, ?, ?, ?)',
            [name, parsedOdcId, parsedPortCount, portInput || '']
        );"""

content = content.replace(old_odp_post, new_odp_post)

old_odc_put = """app.put('/api/odc/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, location, portCount, portInput } = req.body;
        await req.pool.query(
            'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',
            [name, location, portCount || 0, portInput || '', id]
        );"""
        
new_odc_put = """app.put('/api/odc/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, location, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        await req.pool.query(
            'UPDATE odc_list SET name = ?, location = ?, portCount = ?, portInput = ? WHERE id = ?',
            [name, location, parsedPortCount, portInput || '', id]
        );"""

content = content.replace(old_odc_put, new_odc_put)


old_odp_put = """app.put('/api/odp/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, odcId, portCount } = req.body;
        await req.pool.query(
            'UPDATE odp_list SET name = ?, odcId = ?, portCount = ? WHERE id = ?',
            [name, odcId, portCount || 0, id]
        );"""
        
new_odp_put = """app.put('/api/odp/:id', async (req, res) => {
    try {
        const { id } = req.params;
        const { name, odcId, portCount, portInput } = req.body;
        const parsedPortCount = parseInt(portCount) || 0;
        const parsedOdcId = parseInt(odcId) || 0;
        await req.pool.query(
            'UPDATE odp_list SET name = ?, odcId = ?, portCount = ?, portInput = ? WHERE id = ?',
            [name, parsedOdcId, parsedPortCount, portInput || '', id]
        );"""

content = content.replace(old_odp_put, new_odp_put)

with open('VPS/server.js', 'w') as f:
    f.write(content)

