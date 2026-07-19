with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'r') as f:
    content = f.read()

# Replace the fetching block to also fetch all payments
old_fetch = """            val admins = ApiClient.apiService.getAdmins()
            val uangAdmin = try { ApiClient.apiService.getUangDiAdmin() } catch (e: Exception) { emptyList<com.example.ui.data.remote.UangAdminResponse>() }"""

new_fetch = """            val admins = ApiClient.apiService.getAdmins()
            val uangAdmin = try { ApiClient.apiService.getUangDiAdmin() } catch (e: Exception) { emptyList<com.example.ui.data.remote.UangAdminResponse>() }
            val allPayments = try { ApiClient.apiService.getPembayaranHistory() } catch (e: Exception) { emptyList() }"""
content = content.replace(old_fetch, new_fetch)

# Replace jml logic
old_jml = """                val jml = record?.jmlPlggn ?: 0"""
new_jml = """                val jml = allPayments.count { it.admin_name == admin.name }"""
content = content.replace(old_jml, new_jml)

with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'w') as f:
    f.write(content)
print("Updated successfully")
