import re

with open("VPS/add_tenant.js", "r") as f:
    content = f.read()

target = """        const initSql = fs.readFileSync('init.sql', 'utf8');
        await connection.query(`USE \\`${dbName}\\``);
        await connection.query(initSql);"""

rep = """        let initSql = fs.readFileSync('init.sql', 'utf8');
        initSql = initSql.replace(/CREATE DATABASE IF NOT EXISTS app_db;/g, '');
        initSql = initSql.replace(/USE app_db;/g, '');
        
        await connection.query(`USE \\`${dbName}\\``);
        await connection.query(initSql);"""

if target in content:
    content = content.replace(target, rep)
else:
    print("Target not found")

with open("VPS/add_tenant.js", "w") as f:
    f.write(content)

