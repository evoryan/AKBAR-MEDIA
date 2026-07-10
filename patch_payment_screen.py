with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.data.remote.ApiClient
import com.example.ui.data.remote.PaymentRequest
import kotlinx.coroutines.launch
import com.example.ui.data.UserSession
"""
content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\n" + imports)

# We need the user (admin name)
content = content.replace("val totalFormatted = ", "val coroutineScope = rememberCoroutineScope()\n    val currentUser by UserSession.currentUser.collectAsState()\n    val totalFormatted = ")

# Call api when clicking "Ya"
old_click = """                            showConfirmDialog = false
                            onNavigateToSuccess(customerId, totalAmount.toString(), monthsToPay.joinToString(", "))"""
new_click = """                            showConfirmDialog = false
                            coroutineScope.launch {
                                try {
                                    val adminName = currentUser?.name ?: "Admin"
                                    ApiClient.apiService.payBilling(PaymentRequest(customerId, adminName, totalAmount.toDouble()))
                                    onNavigateToSuccess(customerId, totalAmount.toString(), monthsToPay.joinToString(", "))
                                } catch (e: Exception) {}
                            }"""
content = content.replace(old_click, new_click)

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'w') as f:
    f.write(content)
