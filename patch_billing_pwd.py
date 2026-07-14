import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

target = """                TextButton(onClick = {
                    if (cancelPassword == "superadmin") { // Assume superadmin password is "superadmin" or needs to check session?
                        coroutineScope.launch {
                            try {
                                ApiClient.apiService.deleteBilling(DeleteBillingRequest(customerToCancel!!.id))
                                customers = customers.map { if (it.id == customerToCancel!!.id) it.copy(status = "BELUM BAYAR") else it }
                                Toast.makeText(context, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal membatalkan pembayaran", Toast.LENGTH_SHORT).show()
                            }
                            showCancelDialog = false
                            cancelPassword = ""
                            customerToCancel = null
                        }
                    } else {
                        Toast.makeText(context, "Password salah", Toast.LENGTH_SHORT).show()
                    }
                }) {"""

rep = """                TextButton(onClick = {
                    coroutineScope.launch {
                        try {
                            val currentUser = com.example.ui.data.UserSession.currentUser.value
                            if (currentUser != null) {
                                val loginRes = com.example.ui.data.remote.ApiClient.apiService.login(
                                    com.example.ui.data.remote.LoginRequest(currentUser.username, cancelPassword)
                                )
                                if (loginRes.role.name == "SUPER_ADMIN" || loginRes.role.name == "ADMIN") {
                                    ApiClient.apiService.deleteBilling(DeleteBillingRequest(customerToCancel!!.id))
                                    customers = customers.map { if (it.id == customerToCancel!!.id) it.copy(status = "BELUM BAYAR") else it }
                                    Toast.makeText(context, "Pembayaran dibatalkan", Toast.LENGTH_SHORT).show()
                                    showCancelDialog = false
                                    cancelPassword = ""
                                    customerToCancel = null
                                } else {
                                    Toast.makeText(context, "Akses ditolak. Butuh Super Admin", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Password salah", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
