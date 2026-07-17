import re

with open('server_updated.js', 'r') as f:
    content = f.read()

content = content.replace(
    "[name, location, parsedPortCount, portInput || '']",
    "[name, location, parsedPortCount, portInput || '', redaman_in || '', redaman_out || '']"
)

content = content.replace(
    "[name, parsedOdcId, parsedPortCount, portInput || '']",
    "[name, parsedOdcId, parsedPortCount, portInput || '', redaman_in || '', redaman_out || '']"
)

with open('server_updated.js', 'w') as f:
    f.write(content)
