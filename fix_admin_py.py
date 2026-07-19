with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'r') as f:
    content = f.read()

# Fix the function signature
bad_sig = "fun AdminItemCard(onPelangganClick = { selectedAdmin = admin; showDialog = true; coroutineScope.launch { try { isLoadingDialog = true; val allPayments = com.example.ui.data.remote.ApiClient.apiService.getPembayaranHistory(); dialogPayments = allPayments.filter { it.admin_name == admin.name } } catch(e: Exception) {} finally { isLoadingDialog = false } } }, onPelangganClick: () -> Unit = {}, "
good_sig = "fun AdminItemCard(\n    onPelangganClick: () -> Unit = {},\n    "
content = content.replace(bad_sig, good_sig)

with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'w') as f:
    f.write(content)
