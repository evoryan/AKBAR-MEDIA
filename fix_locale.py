import os
import re

directory = 'app/src/main/java/com/example/ui/screens'

for filename in os.listdir(directory):
    if not filename.endswith('.kt'):
        continue
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r') as f:
        content = f.read()

    # Replace Locale("id", "ID") with Locale.forLanguageTag("id-ID")
    content = content.replace('Locale("id", "ID")', 'java.util.Locale.forLanguageTag("id-ID")')
    content = content.replace('java.util.Locale("id", "ID")', 'java.util.Locale.forLanguageTag("id-ID")')

    with open(filepath, 'w') as f:
        f.write(content)

