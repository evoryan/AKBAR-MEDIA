with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
imports_seen = set()

for line in lines:
    if line.startswith("import "):
        if line not in imports_seen:
            imports_seen.add(line)
            new_lines.append(line)
    else:
        new_lines.append(line)

with open('app/src/main/java/com/example/ui/navigation/NavGraph.kt', 'w') as f:
    f.writelines(new_lines)
