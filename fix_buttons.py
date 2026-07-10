import os
import re

directory = 'app/src/main/java/com/example/ui/screens'

for filename in os.listdir(directory):
    if not filename.endswith('.kt'):
        continue
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r') as f:
        content = f.read()

    # Replace containerColor = headerBg inside ButtonDefaults
    content = re.sub(r'(ButtonDefaults\.buttonColors\s*\([^)]*containerColor\s*=\s*)headerBg', r'\1Color(0xFF00FFFF)', content)
    
    # Replace containerColor = headerBg inside Scaffold if any
    content = re.sub(r'Scaffold\(\s*containerColor\s*=\s*headerBg', r'Scaffold(containerColor = bgMain', content)
    
    # Also some isolated containerColor = headerBg that might be DropdownMenu
    content = re.sub(r'(DropdownMenu\s*\([^)]*containerColor\s*=\s*)headerBg', r'\1Color(0xFF11111A)', content)
    
    # Also Box/Row modifier background
    content = re.sub(r'\.background\(headerBg\)', r'.background(Color(0xFF11111A))', content)
    
    with open(filepath, 'w') as f:
        f.write(content)

