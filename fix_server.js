const fs = require('fs');

let server = fs.readFileSync('VPS/server.js', 'utf8');

// 1. Add create table pembukuan
const createTablePembukuan = `
        await masterPool.query(\`CREATE TABLE IF NOT EXISTS pembukuan (
            id INT AUTO_INCREMENT PRIMARY KEY,
            type VARCHAR(50),
            amount DOUBLE,
            description TEXT,
            category VARCHAR(100) DEFAULT 'Lain-lain',
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )\`).catch(e=>{});
        await masterPool.query(\`CREATE TABLE IF NOT EXISTS pengeluaran (
            id INT AUTO_INCREMENT PRIMARY KEY,
            category VARCHAR(100) UNIQUE,
            amount DOUBLE,
            description TEXT,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )\`).catch(e=>{});
`;

server = server.replace('await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});', createTablePembukuan + '\n        await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});');

// 2. Do the same for tenant pools
const createTableTenant = `
            await tPool.query(\`CREATE TABLE IF NOT EXISTS pembukuan (
                id INT AUTO_INCREMENT PRIMARY KEY,
                type VARCHAR(50),
                amount DOUBLE,
                description TEXT,
                category VARCHAR(100) DEFAULT 'Lain-lain',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )\`).catch(e=>{});
            await tPool.query(\`CREATE TABLE IF NOT EXISTS pengeluaran (
                id INT AUTO_INCREMENT PRIMARY KEY,
                category VARCHAR(100) UNIQUE,
                amount DOUBLE,
                description TEXT,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )\`).catch(e=>{});
`;

server = server.replace('await tPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});', createTableTenant + '\n            await tPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});');

fs.writeFileSync('VPS/server.js', server);
