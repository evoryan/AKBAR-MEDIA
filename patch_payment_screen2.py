import re

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'r') as f:
    content = f.read()

old_confirm = """                        onClick = { 
                             showConfirmDialog = false 
                             onNavigateToSuccess(customerId, totalAmount.toString(), monthsToPay.joinToString(", "))
                        },"""
                        
new_confirm = """                        onClick = { 
                             showConfirmDialog = false 
                             coroutineScope.launch {
                                 try {
                                     val req = PaymentRequest(customerId, currentUser?.username ?: "Admin", totalAmount.toDouble())
                                     ApiClient.apiService.payBilling(req)
                                     onNavigateToSuccess(customerId, totalAmount.toString(), monthsToPay.joinToString(", "))
                                 } catch(e: Exception) {
                                     Toast.makeText(context, "Gagal memproses pembayaran: ${e.message}", Toast.LENGTH_SHORT).show()
                                 }
                             }
                        },"""

content = content.replace(old_confirm, new_confirm)

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'w') as f:
    f.write(content)
