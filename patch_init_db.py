import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

init_logic = """
// Auto update all schemas on startup
async function initAllDatabases() {
    try {
        console.log("Checking and updating schemas for all databases...");
        
        // 1. Update master
        await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
        await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        
        // 2. Find all tenant databases
        const [dbs] = await masterPool.query("SHOW DATABASES LIKE 'akbar_%'");
        for (const row of dbs) {
            const dbName = Object.values(row)[0];
            if (dbName === 'akbar_media_master') continue;
            
            console.log(`Updating schema for tenant: ${dbName}`);
            const tPool = getTenantPool(dbName);
            await tPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
            await tPool.query(`ALTER TABLE odc_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0`).catch(e=>{});
            await tPool.query(`ALTER TABLE odp_list ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e=>{});
        }
        console.log("Schema update complete!");
    } catch (e) {
        console.error("Init databases error:", e.message);
    }
}
initAllDatabases();
"""

# Insert right before app.use(tenantContext)
content = content.replace("app.use(tenantContext);", init_logic + "\napp.use(tenantContext);")

with open('VPS/server.js', 'w') as f:
    f.write(content)
