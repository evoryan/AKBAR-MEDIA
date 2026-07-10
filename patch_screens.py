with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    billing = f.read()
billing = billing.replace("fun BillingScreen(onBack", "fun BillingScreen(initialTab: Int = 0, onBack")
billing = billing.replace("var selectedTabIndex by remember { mutableIntStateOf(0) }", "var selectedTabIndex by remember { mutableIntStateOf(initialTab) }")
with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.write(billing)

with open('app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt', 'r') as f:
    sp = f.read()
sp = sp.replace("fun SemuaPembukuanScreen(onBack", "fun SemuaPembukuanScreen(initialType: String = \"Pilih Tipe Pembukuan\", onBack")
sp = sp.replace("var tipePembukuan by remember { mutableStateOf(\"Pilih Tipe Pembukuan\") }", "var tipePembukuan by remember { mutableStateOf(if (initialType.isNotEmpty()) initialType else \"Pilih Tipe Pembukuan\") }")
with open('app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt', 'w') as f:
    f.write(sp)
