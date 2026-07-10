import os
import re

directory = 'app/src/main/java/com/example/ui/screens'

for filename in os.listdir(directory):
    if not filename.endswith('.kt'):
        continue
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r') as f:
        content = f.read()

    # Replace bgMain inside ButtonDefaults to neonCyan if text is Black
    content = re.sub(r'ButtonDefaults\.buttonColors\(\s*containerColor\s*=\s*bgMain\s*,\s*contentColor\s*=\s*Color\.Black\s*\)',
                     r'ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black)', content)
                     
    content = re.sub(r'ButtonDefaults\.buttonColors\(\s*containerColor\s*=\s*bgMain\s*,\s*contentColor\s*=\s*Color\(0xFF000000\)\s*\)',
                     r'ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black)', content)

    # For textMain (white text) on bgMain container, we can use a dark grey or a specific border. Let's use Color(0xFF333333).
    content = re.sub(r'ButtonDefaults\.buttonColors\(\s*containerColor\s*=\s*bgMain\s*,\s*contentColor\s*=\s*textMain\s*\)',
                     r'ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = textMain)', content)
                     
    content = re.sub(r'ButtonDefaults\.buttonColors\(\s*containerColor\s*=\s*bgMain\s*,\s*contentColor\s*=\s*Color\(0xFFFFFFFF\)\s*\)',
                     r'ButtonDefaults.buttonColors(containerColor = Color(0xFF333333), contentColor = Color(0xFFFFFFFF))', content)

    with open(filepath, 'w') as f:
        f.write(content)

