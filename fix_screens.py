import os
import re

def fix_screen(filepath, endpoint_name, list_name):
    with open(filepath, 'r') as f:
        content = f.read()
    
    if "val coroutineScope = rememberCoroutineScope()" not in content:
        # Find where the main scaffold/screen is defined
        content = re.sub(r'(val currentUser.*?)\n', r'\1\n    val coroutineScope = rememberCoroutineScope()\n', content)
    
    # We need to replace the local deletion with an API call + local deletion
    if list_name == "adminList":
        content = content.replace('adminList.remove(item)', f'''
            coroutineScope.launch {{
                try {{
                    ApiClient.apiService.{endpoint_name}(item.id)
                    {list_name}.remove(item)
                }} catch (e: Exception) {{
                }}
            }}
        ''')
    else:
        # others are list assignment like odcList = odcList.filter { it.id != item.id }
        # The exact string used earlier was:
        # {list_name} = {list_name}.filter {{ it.id != item.id }}
        
        target = f"{list_name} = {list_name}.filter {{ it.id != item.id }}"
        replacement = f'''
            coroutineScope.launch {{
                try {{
                    ApiClient.apiService.{endpoint_name}(item.id)
                    {list_name} = {list_name}.filter {{ it.id != item.id }}
                }} catch (e: Exception) {{
                }}
            }}
        '''
        content = content.replace(target, replacement)
        
    # ensure launch is imported
    if "import kotlinx.coroutines.launch" not in content:
        content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport kotlinx.coroutines.launch")

    with open(filepath, 'w') as f:
        f.write(content)

fix_screen('app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt', 'deleteAdmin', 'adminList')
fix_screen('app/src/main/java/com/example/ui/screens/KategoriScreen.kt', 'deleteCategory', 'categoriesList')
fix_screen('app/src/main/java/com/example/ui/screens/InventoryScreen.kt', 'deleteInventory', 'inventoryList')
fix_screen('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'deleteOdc', 'odcList')
fix_screen('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'deleteOdp', 'odpList')
