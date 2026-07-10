filepath = 'app/src/main/java/com/example/ui/screens/CustomersScreen.kt'
with open(filepath, 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if line.strip() == ',' and "Customer(" in lines[i+1]:
        skip = True
        continue
    if skip:
        if line.strip() == ')':
            skip = False
        continue
    new_lines.append(line)

content = "".join(new_lines)
with open(filepath, 'w') as f:
    f.write(content)

