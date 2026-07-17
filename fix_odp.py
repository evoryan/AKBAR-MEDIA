with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if "OutlinedTextField(" in line and "value = redamanIn," in lines[i+1] if i+1 < len(lines) else False:
        if i < 200 or i > 300:  # Only keep the one around 294
            skip = True
            lines_to_skip = 18
            continue
    if skip:
        lines_to_skip -= 1
        if lines_to_skip == 0:
            skip = False
        continue
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.writelines(new_lines)
