import re

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'r') as f:
    content = f.read()

# Make sure Toast is imported
if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp\n", "import androidx.compose.ui.unit.sp\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext\n")

# Find the CustomerItem definition
old_item_def = "fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit, onDeleteCustomer: (Customer) -> Unit) {"
new_item_def = """fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit, onDeleteCustomer: (Customer) -> Unit) {
    val context = LocalContext.current"""
content = content.replace(old_item_def, new_item_def)

# Add the edit icon
old_icons = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onDeleteCustomer(customer) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = errorRed)
                    }
                Box("""
new_icons = """                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { Toast.makeText(context, "Fitur edit pelanggan akan segera hadir", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = neonCyan)
                    }
                    IconButton(onClick = { onDeleteCustomer(customer) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = errorRed)
                    }
                Box("""
content = content.replace(old_icons, new_icons)

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'w') as f:
    f.write(content)
