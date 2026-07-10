import re

filepath = 'app/src/main/java/com/example/ui/navigation/NavGraph.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Replace StockBarangScreen call
old_stock = """            composable<StockBarangRoute> {
                StockBarangScreen(onBack = { navController.popBackStack() })
            }"""

new_stock = """            composable<StockBarangRoute> {
                StockBarangScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToInventory = { navController.navigate(InventoryRoute) },
                    onNavigateToKategori = { navController.navigate(KategoriRoute) },
                    onNavigateToHistory = { navController.navigate(HistoryStockRoute) }
                )
            }
            
            composable<InventoryRoute> {
                InventoryScreen(onBack = { navController.popBackStack() })
            }
            
            composable<KategoriRoute> {
                KategoriScreen(onBack = { navController.popBackStack() })
            }
            
            composable<HistoryStockRoute> {
                HistoryStockScreen(onBack = { navController.popBackStack() })
            }"""

if old_stock in content:
    content = content.replace(old_stock, new_stock)
else:
    print("Could not find old stock composable")

with open(filepath, 'w') as f:
    f.write(content)
