import re

with open("VPS/server.js", "r") as f:
    content = f.read()

global_handlers = """
process.on('uncaughtException', (err) => {
    console.error('UNCAUGHT EXCEPTION:', err);
});

process.on('unhandledRejection', (reason, promise) => {
    console.error('UNHANDLED REJECTION:', reason);
});

const PORT = 4500;
"""

content = content.replace("const PORT = 4500;", global_handlers)

with open("VPS/server.js", "w") as f:
    f.write(content)

