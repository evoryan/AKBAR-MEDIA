import re

filepath = 'VPS/server.js'
with open(filepath, 'r') as f:
    content = f.read()

target_db = """const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
    database: process.env.DB_NAME || 'app_db',"""

replacement_db = """const pool = mysql.createPool({
    host: process.env.DB_HOST || 'localhost',
    user: process.env.DB_USER || 'akbar',
    password: process.env.DB_PASSWORD || '08Delapan',
    database: process.env.DB_NAME || 'app_db',"""

content = content.replace(target_db, replacement_db)

with open(filepath, 'w') as f:
    f.write(content)
