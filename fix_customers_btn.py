filepath = 'app/src/main/java/com/example/ui/screens/CustomersScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """            // Right Column
            Column(horizontalAlignment = Alignment.End) {
                Box("""

replacement = """            // Right Column
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        onDeleteCustomer(customer)
                    }) {
                        Icon(androidx.compose.material.icons.filled.Delete, contentDescription = "Delete", tint = errorRed)
                    }
                    Box("""

content = content.replace(target, replacement)

# ensure we close the Row
target_close = """                    Text(text = customer.id, color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }"""

replacement_close = """                    Text(text = customer.id, color = neonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                }"""

content = content.replace(target_close, replacement_close)

# ensure rememberCoroutineScope is imported
if "import androidx.compose.runtime.rememberCoroutineScope" not in content:
    content = content.replace("import androidx.compose.runtime.remember", "import androidx.compose.runtime.remember\nimport androidx.compose.runtime.rememberCoroutineScope")

with open(filepath, 'w') as f:
    f.write(content)
