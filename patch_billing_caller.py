with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    content = f.read()

# Make sure we use onDeleteClick when calling BillingCustomerItem
old_call = """                            BillingCard(
                                customer = customer,
                                textMain = textMain,
                                textSecondary = textSecondary,
                                cardBg = cardBg,
                                neonCyan = neonCyan,"""
# That was what I tried to replace earlier but failed! Because it's BillingCustomerItem, not BillingCard!

old_real_call = """                            BillingCustomerItem(
                                customer = customer,
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                textMain = textMain,
                                textSecondary = textSecondary,
                                neonCyan = neonCyan,
                                neonPink = neonPink,
                                onPayClick = { onNavigateToPayment(customer.id) },
                                onDetailClick = {
                                    val amount = customer.price.replace(Regex("[^0-9]"), "")
                                    onNavigateToSuccess(customer.id, amount, "Mei 2026")
                                }
                            )"""

new_real_call = """                            BillingCustomerItem(
                                customer = customer,
                                cardBg = cardBg,
                                cardBorder = cardBorder,
                                textMain = textMain,
                                textSecondary = textSecondary,
                                neonCyan = neonCyan,
                                neonPink = neonPink,
                                onPayClick = { onNavigateToPayment(customer.id) },
                                onDetailClick = {
                                    val amount = customer.price.replace(Regex("[^0-9]"), "")
                                    onNavigateToSuccess(customer.id, amount, "Mei 2026")
                                },
                                onDeleteClick = {
                                    coroutineScope.launch {
                                        try {
                                            ApiClient.apiService.deleteBilling(DeleteBillingRequest(customer.id))
                                            fetchCustomers()
                                        } catch (e: Exception) {}
                                    }
                                }
                            )"""

content = content.replace(old_real_call, new_real_call)

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(content)
