import re

filepath = 'app/src/main/java/com/example/ui/data/remote/ApiService.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Fix the broken {id    \n \n } issue.
content = re.sub(r'\{id\s+\}\"', '{id}"', content)

# Wait, the string is @DELETE("api/areas/{id    \n    \n}")
# Let's just fix all @DELETE(".../{id ... }")
content = re.sub(r'\{id\s+\}\"', '{id}"', content)

# Let's do something simpler
lines = content.split('\n')
new_lines = []
for line in lines:
    if line.strip() == '}")':
        # the previous line should have been {id
        new_lines[-1] = new_lines[-1] + '}")'
    elif 'id    ' in line:
        new_lines.append(line.replace('id    ', 'id'))
    elif line.strip() == '':
        new_lines.append(line)
    else:
        new_lines.append(line)

with open(filepath, 'w') as f:
    f.write('\n'.join(new_lines))
