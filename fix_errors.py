import re

# Fix BillingScreen.kt
with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    billing_content = f.read()

billing_content = re.sub(
    r"fun BillingCustomerItem\(\s*customer: Customer,\s*cardBg: Color,\s*cardBorder: Color,\s*textMain: Color,\s*textSecondary: Color,\s*neonCyan: Color,\s*neonPink: Color,\s*onPayClick: \(\) -> Unit,\s*onDetailClick: \(\) -> Unit,\s*onDeleteClick: \(\) -> Unit = \{\}\s*\)\s*\{",
    "@OptIn(ExperimentalFoundationApi::class)\n@Composable\nfun BillingCustomerItem(\n    customer: Customer,\n    cardBg: Color,\n    cardBorder: Color,\n    textMain: Color,\n    textSecondary: Color,\n    neonCyan: Color,\n    neonPink: Color,\n    onPayClick: () -> Unit,\n    onDetailClick: () -> Unit,\n    onDeleteClick: () -> Unit = {},\n    onLongPress: () -> Unit = {}\n) {",
    billing_content,
    flags=re.MULTILINE
)
# Make sure experimental is on BillingScreen composable? It's on BillingCustomerItem, should be fine.
with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(billing_content)

# Fix CompanySettingsScreen.kt
with open("app/src/main/java/com/example/ui/screens/CompanySettingsScreen.kt", "r") as f:
    company_content = f.read()

target = """    var companyName by remember { mutableStateOf(SettingsManager.companyName) }
    var info1 by remember { mutableStateOf(SettingsManager.dashboardInfo1) }
    var info2 by remember { mutableStateOf(SettingsManager.dashboardInfo2) }
    var showSuccess by remember { mutableStateOf(false) }"""

rep = """    var companyName by remember { mutableStateOf(SettingsManager.companyName) }
    var info1 by remember { mutableStateOf(SettingsManager.dashboardInfo1) }
    var info2 by remember { mutableStateOf(SettingsManager.dashboardInfo2) }
    var invoiceFooter by remember { mutableStateOf(SettingsManager.invoiceFooterText) }
    var supportBy by remember { mutableStateOf(SettingsManager.supportByText) }
    var showSuccess by remember { mutableStateOf(false) }"""

if target in company_content:
    company_content = company_content.replace(target, rep)
else:
    # Use regex
    company_content = re.sub(
        r"var showSuccess by remember \{ mutableStateOf\(false\) \}",
        "var invoiceFooter by remember { mutableStateOf(SettingsManager.invoiceFooterText) }\n    var supportBy by remember { mutableStateOf(SettingsManager.supportByText) }\n    var showSuccess by remember { mutableStateOf(false) }",
        company_content
    )

with open("app/src/main/java/com/example/ui/screens/CompanySettingsScreen.kt", "w") as f:
    f.write(company_content)

