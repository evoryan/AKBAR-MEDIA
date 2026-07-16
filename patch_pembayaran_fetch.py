import re

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "r") as f:
    content = f.read()

target = """            realData = res.map { item ->
                PembayaranData(
                    name = item.customer_name ?: "Unknown",
                    initial = (item.customer_name ?: "U").take(1).uppercase(),
                    initialBg = Color(0xFFD1C4E9),
                    phone = item.phone ?: "-",
                    payDate = item.created_at?.take(19)?.replace("T", " ") ?: "",
                    payMonth = "${item.bulan} ${item.tahun}",
                    amount = "Rp. ${String.format("%,d", (item.amount ?: 0.0).toLong()).replace(",", ".")}",
                    area = item.area ?: "-",
                    admin = "Admin By ${item.admin_name ?: "Unknown"}"
                )
            }
        } catch (e: Exception) {"""

rep = """            realData = res.map { item ->
                PembayaranData(
                    name = item.customer_name ?: "Unknown",
                    initial = (item.customer_name ?: "U").take(1).uppercase(),
                    initialBg = Color(0xFFD1C4E9),
                    phone = item.phone ?: "-",
                    payDate = item.created_at?.take(19)?.replace("T", " ") ?: "",
                    payMonth = "${item.bulan} ${item.tahun}",
                    amount = "Rp. ${String.format("%,d", (item.amount ?: 0.0).toLong()).replace(",", ".")}",
                    area = item.area ?: "-",
                    admin = "Admin By ${item.admin_name ?: "Unknown"}"
                )
            }
            // Auto apply default filter on load
            displayedData = realData.filter { item ->
                (filterAdmin == "All" || item.admin == filterAdmin) &&
                (filterArea == "All" || item.area == filterArea)
            }
        } catch (e: Exception) {"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/PembayaranByAdminScreen.kt", "w") as f:
    f.write(content)
