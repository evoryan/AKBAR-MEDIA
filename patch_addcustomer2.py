import re

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "r") as f:
    content = f.read()

pattern1 = r"id = \"\", name = name, phone = phone, area = selectedArea\?\.name \?: \"Semua\", username = selectedSecret\?\.name \?: name\.lowercase\(\)\.replace\(\" \", \"\"\), billingDate = billingDate\.ifEmpty \{ \"1\" \}, registerDate = registerDate, isolateDate = isolateDate, packageName = selectedPackage\?\.name \?: \"\", status = \"BELUM BAYAR\", price = selectedPackage\?\.price\?\.toLong\(\)\?\.let \{ \"Rp\. \" \+ java\.text\.NumberFormat\.getNumberInstance\(java\.util\.Locale\.forLanguageTag\(\"id-ID\"\)\)\.format\(it\) \} \?: \"Rp\. 0\", discount = \"- Dskn : Rp\. 0\", additionalCost1 = additionalCost1, additionalCost2 = additionalCost2, pppoeSecret = selectedSecret\?\.name \?: \"\""
replacement1 = r'id = "", name = name, phone = phone, area = selectedArea?.name ?: "Semua", address = address, username = selectedSecret?.name ?: name.lowercase().replace(" ", ""), billingDate = billingDate.ifEmpty { "1" }, registerDate = registerDate, isolateDate = isolateDate, packageName = selectedPackage?.name ?: "", status = "BELUM BAYAR", price = selectedPackage?.price?.toLong()?.let { "Rp. " + java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(it) } ?: "Rp. 0", discount = "- Dskn : Rp. 0", additionalCost1 = additionalCost1, additionalCost2 = additionalCost2, pppoeSecret = selectedSecret?.name ?: ""'

pattern2 = r"id = \"\", name = name, phone = phone, area = selectedArea\?\.name \?: \"Semua\", username = selectedSecret\?\.name \?: name\.lowercase\(\)\.replace\(\" \", \"\"\), billingDate = billingDate\.ifEmpty \{ \"1\" \}, registerDate = registerDate, isolateDate = isolateDate, packageName = selectedPackage\?\.name \?: \"\", status = \"BELUM BAYAR\", price = selectedPackage\?\.price\?\.toLong\(\)\?\.let \{ \"Rp\. \" \+ java\.text\.NumberFormat\.getNumberInstance\(java\.util\.Locale\.forLanguageTag\(\"id-ID\"\)\)\.format\(it\) \} \?: \"Rp\. 0\", discount = \"- Dskn : Rp\. 0\", additionalCost1 = additionalCost1, additionalCost2 = additionalCost2, pppoeSecret = selectedSecret\?\.name \?: \"\", odpId = selectedOdp\?\.id \?: \"\", odpPort = selectedPort"
replacement2 = r'id = "", name = name, phone = phone, area = selectedArea?.name ?: "Semua", address = address, username = selectedSecret?.name ?: name.lowercase().replace(" ", ""), billingDate = billingDate.ifEmpty { "1" }, registerDate = registerDate, isolateDate = isolateDate, packageName = selectedPackage?.name ?: "", status = "BELUM BAYAR", price = selectedPackage?.price?.toLong()?.let { "Rp. " + java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("id-ID")).format(it) } ?: "Rp. 0", discount = "- Dskn : Rp. 0", additionalCost1 = additionalCost1, additionalCost2 = additionalCost2, pppoeSecret = selectedSecret?.name ?: "", odpId = selectedOdp?.id ?: "", odpPort = selectedPort'

content = re.sub(pattern1, replacement1, content)
content = re.sub(pattern2, replacement2, content)

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "w") as f:
    f.write(content)
