import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

# I want to find the initAllDatabases and change it to safely handle if SHOW DATABASES fails.
# Actually, the user can just manually hit /api/fix-db but wait, if it fails they can't.
# Let's change /api/fix-db and initAllDatabases to ALSO alter the current pool if a query fails.
