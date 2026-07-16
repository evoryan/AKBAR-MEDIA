import re

with open("app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt", "r") as f:
    content = f.read()

# Replace search logic and add state
state_target = """    var currentFilter by remember { mutableStateOf(SecretFilter.ALL) }"""
state_rep = """    var currentFilter by remember { mutableStateOf(SecretFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }"""
content = content.replace(state_target, state_rep)

filter_target = """    val displayedSecrets = allSecrets.filter {
        when (currentFilter) {
            SecretFilter.ALL -> true
            SecretFilter.ONLINE -> it.status == "Online"
            SecretFilter.OFFLINE -> it.status == "Offline"
            SecretFilter.DISABLED -> it.status == "Disabled"
        }
    }"""
filter_rep = """    val displayedSecrets = allSecrets.filter {
        val matchesFilter = when (currentFilter) {
            SecretFilter.ALL -> true
            SecretFilter.ONLINE -> it.status == "Online"
            SecretFilter.OFFLINE -> it.status == "Offline"
            SecretFilter.DISABLED -> it.status == "Disabled"
        }
        val matchesSearch = searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }"""
content = content.replace(filter_target, filter_rep)

# Add search bar
search_target = """            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically"""
search_rep = """            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari Secret...", color = textSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = neonCyan,
                    unfocusedBorderColor = textSecondary.copy(alpha = 0.5f),
                    focusedTextColor = textMain,
                    unfocusedTextColor = textMain,
                    cursorColor = neonCyan
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically"""
content = content.replace(search_target, search_rep)

# Replace secret.name with secret.id for disable/enable
disable_target = """com.example.ui.data.remote.ApiClient.apiService.disableMikrotikSecret(areaId, mapOf("secretId" to secret.name))"""
disable_rep = """com.example.ui.data.remote.ApiClient.apiService.disableMikrotikSecret(areaId, mapOf("secretId" to secret.id))"""
content = content.replace(disable_target, disable_rep)

enable_target = """com.example.ui.data.remote.ApiClient.apiService.enableMikrotikSecret(areaId, mapOf("secretId" to secret.name))"""
enable_rep = """com.example.ui.data.remote.ApiClient.apiService.enableMikrotikSecret(areaId, mapOf("secretId" to secret.id))"""
content = content.replace(enable_target, enable_rep)

with open("app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt", "w") as f:
    f.write(content)
