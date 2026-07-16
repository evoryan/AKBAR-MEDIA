const fs = require('fs');
const path = require('path');

let channelPath = path.join(__dirname, 'node_modules', 'node-routeros', 'dist', 'Channel.js');
if (!fs.existsSync(channelPath)) {
    channelPath = path.join(__dirname, 'node_modules', 'routeros-client', 'node_modules', 'node-routeros', 'dist', 'Channel.js');
}

if (fs.existsSync(channelPath)) {
    let chContent = fs.readFileSync(channelPath, 'utf8');
    let changed = false;

    if (chContent.includes("this.emit('done', this.data);\n                this.close();")) {
        chContent = chContent.replace("case '!empty':\n                this.emit('done', this.data);\n                this.close();\n                break;", "case '!empty':\n                break;");
        changed = true;
    } else if (!chContent.includes("case '!empty':")) {
        chContent = chContent.replace("case '!done':", "case '!empty':\n                break;\n            case '!done':");
        changed = true;
    }

    if (changed) {
        fs.writeFileSync(channelPath, chContent);
        console.log("Auto-patched node-routeros for !empty support successfully at " + channelPath);
    }
}
