filepath = 'VPS/init.sql'
with open(filepath, 'r') as f:
    content = f.read()

target = """CREATE TABLE IF NOT EXISTS areas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    address VARCHAR(255),
    customerCount INT,
    routerIp VARCHAR(50),
    apiDomain VARCHAR(100)
);"""

replacement = """CREATE TABLE IF NOT EXISTS areas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100),
    description VARCHAR(255),
    customerCount INT,
    routerIp VARCHAR(50),
    apiDomain VARCHAR(100),
    mikrotikUser VARCHAR(255),
    mikrotikPassword VARCHAR(255),
    acsUser VARCHAR(255),
    acsPassword VARCHAR(255)
);"""

content = content.replace(target, replacement)
content = content.replace("address VARCHAR(255)", "description VARCHAR(255)")
content = content.replace("INSERT INTO areas (name, address, customerCount, routerIp, apiDomain)", "INSERT INTO areas (name, description, customerCount, routerIp, apiDomain)")

with open(filepath, 'w') as f:
    f.write(content)
