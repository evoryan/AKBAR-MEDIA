import os

directory = 'app/src/main/java/com/example/ui/screens'
for filename in os.listdir(directory):
    if not filename.endswith('.kt'):
        continue
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r') as f:
        content = f.read()

    content = content.replace('java.util.java.util.Locale.forLanguageTag', 'java.util.Locale.forLanguageTag')

    with open(filepath, 'w') as f:
        f.write(content)
