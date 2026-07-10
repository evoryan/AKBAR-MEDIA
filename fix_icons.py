import re

def fix_xml(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Remove the wrongly placed <group> and </group>
    content = re.sub(r'<group[^>]+>', '', content)
    content = content.replace('</group>', '')
    
    # Find the end of <vector ... > properly
    match = re.search(r'<vector[^>]+>', content)
    if match:
        vector_end = match.end()
        group_tag = '\n    <group android:scaleX="0.65" android:scaleY="0.65" android:pivotX="54" android:pivotY="54">'
        content = content[:vector_end] + group_tag + content[vector_end:]
        content = content.replace('</vector>', '    </group>\n</vector>')
        
    with open(filepath, 'w') as f:
        f.write(content)

fix_xml('app/src/main/res/drawable/ic_launcher_foreground.xml')
fix_xml('app/src/main/res/drawable/ic_logo_splash.xml')
