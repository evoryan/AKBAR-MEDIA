import re

with open("app/src/main/java/com/example/ui/screens/EditCustomerScreen.kt", "r") as f:
    content = f.read()

# Replace AddCustomerScreen with EditCustomerScreen
content = content.replace("fun AddCustomerScreen(", "fun EditCustomerScreen(customerId: String, ")

# Initialize customer loading
pattern_init = r"""    var phone by remember \{ mutableStateOf\(\"\"\) \}
    var address by remember \{ mutableStateOf\(\"\"\) \}
    var registerDate by remember \{ mutableStateOf\(\"\"\) \}"""
replacement_init = """    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var registerDate by remember { mutableStateOf("") }
    var originalCustomer by remember { mutableStateOf<Customer?>(null) }
    var isLoadingCustomer by remember { mutableStateOf(true) }"""
content = re.sub(pattern_init, replacement_init, content)

# Change title
content = content.replace("Text(\"Tambah Pelanggan\"", "Text(\"Edit Pelanggan\"")

# Fetch logic
pattern_fetch = r"""    LaunchedEffect\(Unit\) \{
        coroutineScope\.launch \{
            try \{
                areas = ApiClient\.apiService\.getAreas\(\)
                packages = ApiClient\.apiService\.getPackages\(\)
                odps = ApiClient\.apiService\.getOdps\(\)
                customers = ApiClient\.apiService\.getCustomers\(\)
            \} catch \(e: Exception\) \{
                Toast\.makeText\(context, \"Gagal memuat data: \$\{e\.message\}\", Toast\.LENGTH_SHORT\)\.show\(\)
            \}
        \}
    \}"""
replacement_fetch = """    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                areas = ApiClient.apiService.getAreas()
                packages = ApiClient.apiService.getPackages()
                odps = ApiClient.apiService.getOdps()
                customers = ApiClient.apiService.getCustomers()
                val cust = customers.find { it.id == customerId }
                if (cust != null) {
                    originalCustomer = cust
                    name = cust.name
                    phone = cust.phone
                    address = cust.address ?: ""
                    registerDate = cust.registerDate ?: ""
                    billingDate = cust.billingDate
                    isolateDate = cust.isolateDate ?: ""
                    selectedArea = areas.find { it.name == cust.area }
                    selectedPackage = packages.find { it.name == cust.packageName }
                    additionalCost1 = cust.additionalCost1 ?: ""
                    additionalCost2 = cust.additionalCost2 ?: ""
                    if (!cust.odpId.isNullOrEmpty()) {
                        selectedOdp = odps.find { it.id.toString() == cust.odpId }
                        selectedPort = cust.odpPort ?: ""
                    }
                    if (!cust.pppoeSecret.isNullOrEmpty()) {
                        secretSearchQuery = cust.pppoeSecret
                        selectedSecret = com.example.ui.data.PPPoESecret(cust.pppoeSecret, "","","","", "") 
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoadingCustomer = false
            }
        }
    }"""
content = re.sub(pattern_fetch, replacement_fetch, content)

# Replace addCustomer with updateCustomer
pattern_save = r"""                                        val newCust = Customer\(
                                            id = \"\", name = name, phone = phone, area = selectedArea\?\.name \?: \"Semua\", address = address, username = selectedSecret\?\.name \?: name\.lowercase\(\)\.replace\(\" \", \"\"\), billingDate = billingDate\.ifEmpty \{ \"1\" \}, registerDate = registerDate, isolateDate = isolateDate, packageName = selectedPackage\?\.name \?: \"\", status = \"BELUM BAYAR\", price = selectedPackage\?\.price\?\.toLong\(\)\?\.let \{ \"Rp\. \" \+ java\.text\.NumberFormat\.getNumberInstance\(java\.util\.Locale\.forLanguageTag\(\"id-ID\"\)\)\.format\(it\) \} \?: \"Rp\. 0\", discount = \"- Dskn : Rp\. 0\", additionalCost1 = additionalCost1, additionalCost2 = additionalCost2, pppoeSecret = selectedSecret\?\.name \?: \"\", odpId = selectedOdp\?\.id \?: \"\", odpPort = selectedPort
                                        \)
                                        ApiClient\.apiService\.addCustomer\(newCust\)
                                        Toast\.makeText\(context, \"Pelanggan berhasil ditambahkan!\", Toast\.LENGTH_SHORT\)\.show\(\)"""
replacement_save = """                                        val updatedCust = Customer(
                                            id = customerId, name = name, phone = phone, area = selectedArea?.name ?: originalCustomer?.area ?: "Semua", address = address, username = selectedSecret?.name ?: originalCustomer?.username ?: name.lowercase().replace(" ", ""), billingDate = billingDate.ifEmpty { originalCustomer?.billingDate ?: "1" }, registerDate = registerDate, isolateDate = isolateDate, packageName = selectedPackage?.name ?: originalCustomer?.packageName ?: "", status = originalCustomer?.status ?: "BELUM BAYAR", price = selectedPackage?.price?.toLong()?.let { "Rp. " + java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(it) } ?: originalCustomer?.price ?: "Rp. 0", discount = originalCustomer?.discount ?: "- Dskn : Rp. 0", additionalCost1 = additionalCost1, additionalCost2 = additionalCost2, pppoeSecret = selectedSecret?.name ?: originalCustomer?.pppoeSecret ?: "", odpId = selectedOdp?.id?.toString() ?: originalCustomer?.odpId, odpPort = selectedPort.ifEmpty { originalCustomer?.odpPort ?: "" }
                                        )
                                        ApiClient.apiService.updateCustomer(customerId, updatedCust)
                                        Toast.makeText(context, "Pelanggan berhasil diupdate!", Toast.LENGTH_SHORT).show()"""
content = re.sub(pattern_save, replacement_save, content)

# Hide content while loading
content = content.replace("Scaffold(", """if (isLoadingCustomer) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primaryPurple)
        }
        return
    }
    Scaffold(""")

with open("app/src/main/java/com/example/ui/screens/EditCustomerScreen.kt", "w") as f:
    f.write(content)

