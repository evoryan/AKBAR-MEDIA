import re

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'r') as f:
    content = f.read()

old_fun = """fun CustomerDetailScreen(customerId: String, onBack: () -> Unit, onNavigateToPayment: (String) -> Unit) {"""
new_fun = """fun CustomerDetailScreen(customerId: String, onBack: () -> Unit, onNavigateToPayment: (String) -> Unit, onNavigateToAcs: (String) -> Unit = {}) {"""

content = content.replace(old_fun, new_fun)

old_btn = """                            Button(
                                onClick = {
                                    if (acsDevice != null) {
                                        Toast.makeText(context, "ACS Data:\\nRxPower: ${acsDevice?.rxPower}\\nSSID: ${acsDevice?.ssid}\\nConnected: ${acsDevice?.connectedUsers}", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Data ACS tidak ditemukan untuk username ini", Toast.LENGTH_SHORT).show()
                                    }
                                },"""

new_btn = """                            Button(
                                onClick = {
                                    if (!customer?.pppoeSecret.isNullOrBlank()) {
                                        onNavigateToAcs(customer!!.pppoeSecret!!)
                                    } else {
                                        Toast.makeText(context, "Data ACS tidak ditemukan untuk username ini", Toast.LENGTH_SHORT).show()
                                    }
                                },"""

content = content.replace(old_btn, new_btn)

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'w') as f:
    f.write(content)
