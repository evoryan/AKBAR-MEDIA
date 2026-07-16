import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

# Remove any imports before the package declaration
if content.startswith("import "):
    # extract all imports at the very beginning
    imports = []
    lines = content.split('\n')
    idx = 0
    while lines[idx].startswith("import "):
        imports.append(lines[idx])
        idx += 1
    
    # find package declaration
    pkg_idx = -1
    for i in range(idx, len(lines)):
        if lines[i].startswith("package "):
            pkg_idx = i
            break
            
    if pkg_idx != -1:
        pkg_line = lines[pkg_idx]
        lines[pkg_idx] = ""
        
        # reconstruct
        new_content = pkg_line + "\n\n" + "\n".join(imports) + "\n" + "\n".join(lines[idx:])
        content = new_content

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)

