const fs = require('fs');
const path = require('path');
let channelPath = path.join(__dirname, 'VPS', 'node_modules', 'node-routeros', 'dist', 'Channel.js');
let content = fs.readFileSync(channelPath, 'utf8');
content = content.replace("case '!empty':\n                this.emit('done', this.data);\n                this.close();\n                break;", "case '!empty':\n                break;");
fs.writeFileSync(channelPath, content);
