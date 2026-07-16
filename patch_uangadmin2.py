import re

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "r") as f:
    content = f.read()

target = """            val uangAdmin = try { ApiClient.apiService.getUangDiAdmin() } catch (e: Exception) { emptyList<com.example.ui.data.remote.UangAdminResponse>() }
            
            val mapped = admins.map { admin ->
                val amount = uangAdmin.find { it.adminName == admin.name }?.totalAmount ?: 0.0
                val formattedAmount = "Rp. ${String.format("%,d", amount.toLong()).replace(",", ".")}"
                val jml = uangAdmin.find { it.adminName == admin.name }?.jmlPlggn ?: 0
                AdminData(
                    name = admin.name,
                    totalDiterima = formattedAmount,
                    setor = "Rp. 0",
                    sisa = formattedAmount,
                    sisaColor = warningYellow,
                    persentase = "100%",
                    pengeluaran = "Rp. 0",
                    persentaseSisa = "100%",
                    jmlPlggn = jml.toString()
                )
            }
            adminList = mapped"""

rep = """            val uangAdmin = try { ApiClient.apiService.getUangDiAdmin() } catch (e: Exception) { emptyList<com.example.ui.data.remote.UangAdminResponse>() }
            
            val mapped = admins.map { admin ->
                val record = uangAdmin.find { it.adminName == admin.name }
                val diterima = record?.totalDiterima ?: 0.0
                val setor = record?.setor ?: 0.0
                val pengeluaran = record?.pengeluaran ?: 0.0
                val sisa = diterima - setor - pengeluaran
                
                val formattedDiterima = "Rp. ${String.format("%,d", diterima.toLong()).replace(",", ".")}"
                val formattedSetor = "Rp. ${String.format("%,d", setor.toLong()).replace(",", ".")}"
                val formattedPengeluaran = "Rp. ${String.format("%,d", pengeluaran.toLong()).replace(",", ".")}"
                val formattedSisa = "Rp. ${String.format("%,d", sisa.toLong()).replace(",", ".")}"
                
                val jml = record?.jmlPlggn ?: 0
                AdminData(
                    name = admin.name,
                    totalDiterima = formattedDiterima,
                    setor = formattedSetor,
                    sisa = formattedSisa,
                    sisaColor = if (sisa < 0) errorRed else if (sisa == 0.0) successGreen else warningYellow,
                    persentase = "100%", // could be calculated if we have a target
                    pengeluaran = formattedPengeluaran,
                    persentaseSisa = "100%",
                    jmlPlggn = jml.toString()
                )
            }
            adminList = mapped"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "w") as f:
    f.write(content)
