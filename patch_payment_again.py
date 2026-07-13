import re

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'r') as f:
    content = f.read()

old_confirm = """                    Button(
                        onClick = { 
                            showConfirmDialog = false 
                            onNavigateToSuccess(customerId, totalAmount.toString(), monthsToPay.joinToString(", "))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black)
                    ) {"""

new_confirm = """                    Button(
                        onClick = { 
                            showConfirmDialog = false 
                            coroutineScope.launch {
                                try {
                                    val req = com.example.ui.data.remote.PaymentRequest(
                                        customerId = customerId,
                                        adminName = currentUser?.name ?: "Admin",
                                        totalAmount = totalAmount.toDouble()
                                    )
                                    ApiClient.apiService.payBilling(req)
                                    onNavigateToSuccess(customerId, totalAmount.toString(), monthsToPay.joinToString(", "))
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Pembayaran gagal: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black)
                    ) {"""

content = content.replace(old_confirm, new_confirm)

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'w') as f:
    f.write(content)
