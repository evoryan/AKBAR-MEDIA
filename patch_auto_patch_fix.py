import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Replace the bad patch with the correct one
content = content.replace("case '!empty':\\n                this.emit('done', this.data);\\n                this.close();\\n                break;", "case '!empty':\\n                break;")

with open("VPS/server.js", "w") as f:
    f.write(content)
