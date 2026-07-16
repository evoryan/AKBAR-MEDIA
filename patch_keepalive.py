import re

with open("VPS/server.js", "r") as f:
    content = f.read()

old_listen = """app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});"""

new_listen = """const server = app.listen(PORT, '0.0.0.0', () => {
    console.log(`Server running on port ${PORT}`);
});
server.keepAliveTimeout = 65000;
server.headersTimeout = 66000;"""

content = content.replace(old_listen, new_listen)

with open("VPS/server.js", "w") as f:
    f.write(content)
