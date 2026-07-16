import re

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "r") as f:
    content = f.read()

pattern1 = r"""fun CustomerItem\(customer: Customer, onNavigateToCustomerDetail: \(String\) -> Unit, onDeleteCustomer: \(Customer\) -> Unit, onIsolirCustomer: \(Customer\) -> Unit = \{\}\) \{"""
replacement1 = """fun CustomerItem(customer: Customer, onNavigateToCustomerDetail: (String) -> Unit, onDeleteCustomer: (Customer) -> Unit, onIsolirCustomer: (Customer) -> Unit = {}, onEditCustomer: (String) -> Unit = {}) {"""
content = re.sub(pattern1, replacement1, content)

pattern2 = r"""IconButton\(onClick = \{ onNavigate\(com\.example\.ui\.navigation\.EditCustomerRoute\(customer\.id\)\) \}\) \{"""
replacement2 = """IconButton(onClick = { onEditCustomer(customer.id) }) {"""
content = re.sub(pattern2, replacement2, content)

pattern3 = r"""                        CustomerItem\(customer = it, onNavigateToCustomerDetail = \{ id -> onNavigate\(com\.example\.ui\.navigation\.CustomerDetailRoute\(id\)\) \}, onDeleteCustomer = \{ c ->
                            customerToDelete = c
                            showDeleteDialog = true
                        \}, onIsolirCustomer = \{ c ->
                            customerToIsolir = c
                            showIsolirDialog = true
                        \}\)"""
replacement3 = """                        CustomerItem(customer = it, onNavigateToCustomerDetail = { id -> onNavigate(com.example.ui.navigation.CustomerDetailRoute(id)) }, onDeleteCustomer = { c ->
                            customerToDelete = c
                            showDeleteDialog = true
                        }, onIsolirCustomer = { c ->
                            customerToIsolir = c
                            showIsolirDialog = true
                        }, onEditCustomer = { id ->
                            onNavigate(com.example.ui.navigation.EditCustomerRoute(id))
                        })"""
content = re.sub(pattern3, replacement3, content)

with open("app/src/main/java/com/example/ui/screens/CustomersScreen.kt", "w") as f:
    f.write(content)
