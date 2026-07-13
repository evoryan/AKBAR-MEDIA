import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# Add a function to check and alter table
alter_logic = """
// Auto-update schema to avoid errors if user doesn't run init.sql
async function updateSchema() {
    try {
        await masterPool.query(`ALTER TABLE odc_list ADD COLUMN portCount INT DEFAULT 0, ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e => { /* Ignore duplicate column error */ });
        await masterPool.query(`ALTER TABLE odp_list ADD COLUMN portCount INT DEFAULT 0, ADD COLUMN portInput VARCHAR(100) DEFAULT ''`).catch(e => { /* Ignore */ });
        console.log("Schema checked/updated.");
    } catch(e) {
        console.error("Schema update error:", e.message);
    }
}
updateSchema();

"""

content = content.replace("app.use(tenantContext);", alter_logic + "app.use(tenantContext);")

with open('VPS/server.js', 'w') as f:
    f.write(content)
