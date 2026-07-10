import re

circle_path = 'M 79,24 a 6,6 0 1,0 12,0 a 6,6 0 1,0 -12,0'

with open('app/src/main/res/drawable/ic_launcher_foreground.xml', 'r') as f:
    content = f.read()

content = re.sub(r'M 85,24 A 6,6 0 1,1 85,23\.9', circle_path, content)

with open('app/src/main/res/drawable/ic_launcher_foreground.xml', 'w') as f:
    f.write(content)
