filepath = 'VPS/server.js'
with open(filepath, 'r') as f:
    lines = f.readlines()

listen_lines = []
new_lines = []

in_listen = False
for line in lines:
    if line.startswith("const PORT = ") or line.startswith("app.listen("):
        in_listen = True
        listen_lines.append(line)
        continue
    
    if in_listen:
        listen_lines.append(line)
        if line.strip() == "});":
            in_listen = False
        continue
        
    new_lines.append(line)

new_lines.extend(listen_lines)

with open(filepath, 'w') as f:
    f.writelines(new_lines)
