import re

with open("app/src/main/java/com/example/ui/screens/CompanySettingsScreen.kt", "r") as f:
    content = f.read()

target = """                onClick = {
                    SettingsManager.companyName = companyName
                    SettingsManager.dashboardInfo1 = info1
                    SettingsManager.dashboardInfo2 = info2
                    showSuccess = true
                },"""

rep = """                onClick = {
                    SettingsManager.companyName = companyName
                    SettingsManager.dashboardInfo1 = info1
                    SettingsManager.dashboardInfo2 = info2
                    SettingsManager.invoiceFooterText = invoiceFooter
                    SettingsManager.supportByText = supportBy
                    showSuccess = true
                },"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/CompanySettingsScreen.kt", "w") as f:
    f.write(content)
