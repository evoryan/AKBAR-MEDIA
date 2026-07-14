import re

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "r") as f:
    content = f.read()

target = """            ReceiptRow("Keterangan", "L U N A S", successGreen, isBold = true)
            DividerLine()
            ReceiptRow("Admin By", com.example.ui.data.SettingsManager.companyName, textMain)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Support By:\nToko Ana, PT.Telkom, PT.Citra Selaras Terabit,",
                color = textSecondary,"""

rep = """            ReceiptRow("Keterangan", com.example.ui.data.SettingsManager.invoiceFooterText, successGreen, isBold = true)
            DividerLine()
            ReceiptRow("Admin By", com.example.ui.data.SettingsManager.companyName, textMain)
            
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Support By:\n" + com.example.ui.data.SettingsManager.supportByText,
                color = textSecondary,"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "w") as f:
    f.write(content)
