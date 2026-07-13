import re

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'r') as f:
    content = f.read()

# Change signature
old_sig = "fun CustomerDetailScreen(customerId: String, onBack: () -> Unit) {"
new_sig = "fun CustomerDetailScreen(customerId: String, onBack: () -> Unit, onNavigateToPayment: (String) -> Unit) {"
content = content.replace(old_sig, new_sig)

# Change onClick
old_btn = "onClick = { showPaymentDialog = true },"
new_btn = "onClick = { onNavigateToPayment(customerId) },"
content = content.replace(old_btn, new_btn)

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'w') as f:
    f.write(content)
