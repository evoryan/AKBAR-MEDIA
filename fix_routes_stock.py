import re

filepath = 'app/src/main/java/com/example/ui/navigation/Routes.kt'
with open(filepath, 'r') as f:
    content = f.read()

new_routes = """
@Serializable
object InventoryRoute

@Serializable
object KategoriRoute

@Serializable
object HistoryStockRoute
"""

content = content + new_routes

with open(filepath, 'w') as f:
    f.write(content)
