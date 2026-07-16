import re

with open("VPS/server.js", "r") as f:
    content = f.read()

auto_patch_code = """
// --- AUTO PATCH NODE-ROUTEROS FOR !EMPTY BUG ---
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
            if (!chContent.includes("case '!empty':")) {
                chContent = chContent.replace("case '!done':", "case '!empty':\\n                this.emit('done', this.data);\\n                this.close();\\n                break;\\n            case '!done':");
                fs.writeFileSync(channelPath, chContent);
                console.log("Auto-patched node-routeros for !empty support at " + channelPath);
            } else if (chContent.includes("case '!empty':\\n                break;")) {
                chContent = chContent.replace("case '!empty':\\n                break;\\n            case '!done':", "case '!empty':\\n                this.emit('done', this.data);\\n                this.close();\\n                break;\\n            case '!done':");
                fs.writeFileSync(channelPath, chContent);
                console.log("Auto-fixed node-routeros !empty patch at " + channelPath);
            }
        }
    } catch (err) {
        console.error("Failed to auto-patch node-routeros:", err);
    }
}
patchNodeRouterOS();
// ------------------------------------------------

const express = require('express');
"""

content = content.replace("const express = require('express');", auto_patch_code)

with open("VPS/server.js", "w") as f:
    f.write(content)

