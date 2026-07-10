filepath = 'app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                    Button(
                        onClick = { onBack() },"""

replacement = """                    val coroutineScope = rememberCoroutineScope()
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val newCust = Customer(
                                        id = "",
                                        name = name,
                                        phone = phone,
                                        area = "Semua", // fallback, but can be updated if there is area selector
                                        username = name.lowercase().replace(" ", ""),
                                        billingDate = "10",
                                        status = "BELUM BAYAR",
                                        price = "Rp. 150.000",
                                        discount = "- Dskn : Rp. 0"
                                    )
                                    com.example.ui.data.remote.ApiClient.apiService.addCustomer(newCust)
                                    onBack()
                                } catch (e: Exception) {
                                }
                            }
                        },"""

content = content.replace(target, replacement)

# ensure we import rememberCoroutineScope, launch, Customer
if "import androidx.compose.runtime.rememberCoroutineScope" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.compose.runtime.rememberCoroutineScope\nimport kotlinx.coroutines.launch")

if "import com.example.ui.screens.Customer" not in content:
    content = content.replace("package com.example.ui.screens", "package com.example.ui.screens\nimport com.example.ui.screens.Customer")

with open(filepath, 'w') as f:
    f.write(content)
