import re

with open("VPS/server.js", "r") as f:
    content = f.read()

old_block_pattern = r"// --- AUTO PATCH NODE-ROUTEROS FOR !EMPTY BUG \(FIXED\) ---.*?// ------------------------------------------------\n"
content = re.sub(old_block_pattern, "", content, flags=re.DOTALL)

new_patch_logic = """// --- AUTO PATCH NODE-ROUTEROS FOR !EMPTY BUG (FIXED) ---
const fs = require('fs');
const path = require('path');

function patchNodeRouterOS() {
    try {
        let channelPath = path.join(__dirname, 'node_modules', 'node-routeros', 'dist', 'Channel.js');
        if (!fs.existsSync(channelPath)) {
            channelPath = path.join(__dirname, 'node_modules', 'routeros-client', 'node_modules', 'node-routeros', 'dist', 'Channel.js');
        }

        if (fs.existsSync(channelPath)) {
            let chContent = fs.readFileSync(channelPath, 'utf8');
            let changed = false;

            // If it has the BAD patch with this.close()
            if (chContent.includes("this.emit('done', this.data);\\n                this.close();")) {
                chContent = chContent.replace("case '!empty':\\n                this.emit('done', this.data);\\n                this.close();\\n                break;", "case '!empty':\\n                break;");
                changed = true;
            } 
            // If it has no !empty support at all
            else if (!chContent.includes("case '!empty':")) {
                chContent = chContent.replace("case '!done':", "case '!empty':\\n                break;\\n            case '!done':");
                changed = true;
            }

            if (changed) {
                fs.writeFileSync(channelPath, chContent);
                console.log("Auto-patched node-routeros for !empty support successfully at " + channelPath);
            }
        }
    } catch (err) {
        console.error("Failed to auto-patch node-routeros:", err);
    }
}
patchNodeRouterOS();
// ------------------------------------------------
"""

content = new_patch_logic + content

with open("VPS/server.js", "w") as f:
    f.write(content)
