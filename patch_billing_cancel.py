import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

target = """                                onDetailClick = {
                                    val amount = customer.price.replace(Regex("[^0-9]"), "")
                                    onNavigateToSuccess(customer.id, amount, "Mei 2026")
                                }
                            )"""

rep = """                                onDetailClick = {
                                    val amount = customer.price.replace(Regex("[^0-9]"), "")
                                    onNavigateToSuccess(customer.id, amount, "Mei 2026")
                                },
                                onLongPress = {
                                    coroutineScope.launch {
                                        try {
                                            ApiClient.apiService.deleteBilling(DeleteBillingRequest(customer.id))
                                            fetchCustomers()
                                            Toast.makeText(context, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Gagal membatalkan pembayaran", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
