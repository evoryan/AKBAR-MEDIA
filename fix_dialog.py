with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    lines = f.readlines()

in_dialog = False
dialog_lines = []
new_lines = []
for line in lines:
    if "if (showAddDialog) {" in line:
        in_dialog = True
        dialog_lines.append(line)
        continue
    
    if in_dialog:
        dialog_lines.append(line)
        if "    }" in line and "properties =" not in line and "modifier =" not in line and "dismissButton" not in line and "confirmButton" not in line:
            # Wait, this might be tricky to match exactly.
            pass
        # I'll just match the end by counting braces
        if line.strip() == "}" and len([l for l in dialog_lines if "{" in l]) == len([l for l in dialog_lines if "}" in l]):
            in_dialog = False
    else:
        new_lines.append(line)

# Let's use a simpler approach: read file, extract the block using string replace.
