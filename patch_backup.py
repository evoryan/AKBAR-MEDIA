import re

with open("app/src/main/java/com/example/ui/screens/BackupRestoreScreen.kt", "r") as f:
    content = f.read()

target1 = """                    context.contentResolver.openOutputStream(it)?.use { os ->
                        val writer = CSVWriter(OutputStreamWriter(os))
                        writer.writeNext(arrayOf("id", "name", "phone", "address", "paket", "price", "status", "jatuh_tempo", "odpId", "port", "pppoeSecret", "area", "nik"))
                        for (c in customers) {
                            writer.writeNext(arrayOf(c.id, c.name, c.phone, c.address, c.paket, c.price, c.status, c.jatuh_tempo, c.odpId ?: "", c.port ?: "", c.pppoeSecret ?: "", c.area, c.nik ?: ""))
                        }
                        writer.close()
                    }"""

rep1 = """                    context.contentResolver.openOutputStream(it)?.use { os ->
                        val writer = CSVWriter(OutputStreamWriter(os))
                        writer.writeNext(arrayOf("id", "name", "phone", "area", "username", "billingDate", "status", "price", "discount", "registerDate", "isolateDate", "packageName", "additionalCost1", "additionalCost2", "pppoeSecret", "odpId", "odpPort"))
                        for (c in customers) {
                            writer.writeNext(arrayOf(c.id, c.name, c.phone, c.area, c.username, c.billingDate, c.status, c.price, c.discount, c.registerDate ?: "", c.isolateDate ?: "", c.packageName ?: "", c.additionalCost1 ?: "", c.additionalCost2 ?: "", c.pppoeSecret ?: "", c.odpId ?: "", c.odpPort ?: ""))
                        }
                        writer.close()
                    }"""

target2 = """                                if (row.size >= 13) {
                                    val req = mapOf(
                                        "name" to row[1],
                                        "phone" to row[2],
                                        "address" to row[3],
                                        "paket" to row[4],
                                        "price" to row[5],
                                        "status" to row[6],
                                        "jatuh_tempo" to row[7],
                                        "odpId" to row[8],
                                        "port" to row[9],
                                        "pppoeSecret" to row[10],
                                        "area" to row[11],
                                        "nik" to row[12]
                                    )
                                    // Normally we should batch or create individual, let's create individual
                                    ApiClient.apiService.addCustomer(req)
                                }"""

rep2 = """                                if (row.size >= 17) {
                                    val req = com.example.ui.screens.Customer(
                                        name = row[1],
                                        phone = row[2],
                                        area = row[3],
                                        username = row[4],
                                        billingDate = row[5],
                                        status = row[6],
                                        price = row[7],
                                        discount = row[8],
                                        registerDate = row[9],
                                        isolateDate = row[10],
                                        packageName = row[11],
                                        additionalCost1 = row[12],
                                        additionalCost2 = row[13],
                                        pppoeSecret = row[14],
                                        odpId = row[15],
                                        odpPort = row[16]
                                    )
                                    ApiClient.apiService.addCustomer(req)
                                }"""

content = content.replace(target1, rep1)
content = content.replace(target2, rep2)

with open("app/src/main/java/com/example/ui/screens/BackupRestoreScreen.kt", "w") as f:
    f.write(content)
print("Patched BackupRestoreScreen.kt")
