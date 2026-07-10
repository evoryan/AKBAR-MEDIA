filepath = '/app/applet/app/src/main/java/com/example/ui/screens/InventoryScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('editItem!!.stock', '(editItem?.stock ?: 0)')
content = content.replace('editItem!!.id', '(editItem?.id ?: "")')

with open(filepath, 'w') as f:
    f.write(content)
