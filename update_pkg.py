import json
with open('VPS/package.json', 'r') as f:
    data = json.load(f)

data['dependencies']['routeros-client'] = '^1.3.1'

with open('VPS/package.json', 'w') as f:
    json.dump(data, f, indent=2)
