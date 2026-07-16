import re

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "r") as f:
    content = f.read()

pattern = r"""                        \}
                    \}\)

                \}"""
replacement = """                        }
                    }, onEditCustomer = { id ->
                        onNavigate(com.example.ui.navigation.EditCustomerRoute(id))
                    })

                }"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "w") as f:
    f.write(content)
