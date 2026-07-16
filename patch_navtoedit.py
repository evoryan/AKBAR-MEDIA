import re

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "r") as f:
    content = f.read()

pattern = r"""IconButton\(onClick = \{ Toast\.makeText\(context, \"Fitur edit pelanggan akan segera hadir\", Toast\.LENGTH_SHORT\)\.show\(\) \}\) \{
                        Icon\(Icons\.Default\.Edit, contentDescription = \"Edit\", tint = neonCyan\)
                    \}"""
replacement = """IconButton(onClick = { onNavigate(com.example.ui.navigation.EditCustomerRoute(customer.id)) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = neonCyan)
                    }"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "w") as f:
    f.write(content)
