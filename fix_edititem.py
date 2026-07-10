import os
import glob

def fix_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    content = content.replace('editItem!!.id', '(editItem?.id ?: "")')
    content = content.replace('editItem!!.copy', '(editItem?.copy)')
    with open(filepath, 'w') as f:
        f.write(content)

for root, _, files in os.walk('/app/applet/app/src/main/java/com/example/ui/screens'):
    for file in files:
        if file.endswith('.kt'):
            fix_file(os.path.join(root, file))

