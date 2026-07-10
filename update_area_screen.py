import re

filepath = 'app/src/main/java/com/example/ui/screens/AreaScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Update Area data class
target_dataclass = """data class Area(
    val id: String,
    val name: String,
    val description: String = "",
    val customerCount: Int = 0,
    val routerIp: String = "",
    val apiDomain: String = ""
)"""

replacement_dataclass = """data class Area(
    val id: String,
    val name: String,
    val description: String = "",
    val customerCount: Int = 0,
    val routerIp: String = "",
    val apiDomain: String = "",
    val mikrotikUser: String = "",
    val mikrotikPassword: String = "",
    val acsUser: String = "",
    val acsPassword: String = ""
)"""
content = content.replace(target_dataclass, replacement_dataclass)

# Update AreaFormDialog signature and state variables
target_dialog_start = """    var mikrotikApi by remember { mutableStateOf(initialArea?.routerIp ?: "") }
    var acsApi by remember { mutableStateOf(initialArea?.apiDomain ?: "") }
    var testResult by remember { mutableStateOf<String?>(null) }"""

replacement_dialog_start = """    var mikrotikApi by remember { mutableStateOf(initialArea?.routerIp ?: "") }
    var mikrotikUser by remember { mutableStateOf(initialArea?.mikrotikUser ?: "") }
    var mikrotikPassword by remember { mutableStateOf(initialArea?.mikrotikPassword ?: "") }
    var acsApi by remember { mutableStateOf(initialArea?.apiDomain ?: "") }
    var acsUser by remember { mutableStateOf(initialArea?.acsUser ?: "") }
    var acsPassword by remember { mutableStateOf(initialArea?.acsPassword ?: "") }
    var testResult by remember { mutableStateOf<String?>(null) }"""
content = content.replace(target_dialog_start, replacement_dialog_start)

# Add new input fields into the form content
# After Mikrotik API input:
target_mikrotik_input = """                    OutlinedTextField(
                        value = mikrotikApi,
                        onValueChange = { mikrotikApi = it },
                        label = { Text("Alamat API Mikrotik (IP:Port)", color = textSecondary) },
                        placeholder = { Text("Contoh: 192.168.1.1:8728", color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )"""

replacement_mikrotik_input = """                    OutlinedTextField(
                        value = mikrotikApi,
                        onValueChange = { mikrotikApi = it },
                        label = { Text("Alamat API Mikrotik (IP:Port)", color = textSecondary) },
                        placeholder = { Text("Contoh: 192.168.1.1:8728", color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mikrotikUser,
                        onValueChange = { mikrotikUser = it },
                        label = { Text("Username API Mikrotik", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mikrotikPassword,
                        onValueChange = { mikrotikPassword = it },
                        label = { Text("Password API Mikrotik", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )"""
content = content.replace(target_mikrotik_input, replacement_mikrotik_input)

# After ACS API input:
target_acs_input = """                    OutlinedTextField(
                        value = acsApi,
                        onValueChange = { acsApi = it },
                        label = { Text("Alamat ACS (URL)", color = textSecondary) },
                        placeholder = { Text("Contoh: http://192.168.1.1:7557", color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )"""

replacement_acs_input = """                    OutlinedTextField(
                        value = acsApi,
                        onValueChange = { acsApi = it },
                        label = { Text("Alamat ACS (URL)", color = textSecondary) },
                        placeholder = { Text("Contoh: http://192.168.1.1:7557", color = textSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = acsUser,
                        onValueChange = { acsUser = it },
                        label = { Text("Username ACS", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = acsPassword,
                        onValueChange = { acsPassword = it },
                        label = { Text("Password ACS", color = textSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = neonCyan, unfocusedBorderColor = textSecondary,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        ),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )"""
content = content.replace(target_acs_input, replacement_acs_input)

# On save logic
target_save = """                                onSave(Area(
                                    id = "",
                                    name = name,
                                    description = description,
                                    routerIp = mikrotikApi,
                                    apiDomain = acsApi,
                                    customerCount = initialArea?.customerCount ?: 0
                                ))"""

replacement_save = """                                onSave(Area(
                                    id = "",
                                    name = name,
                                    description = description,
                                    routerIp = mikrotikApi,
                                    apiDomain = acsApi,
                                    customerCount = initialArea?.customerCount ?: 0,
                                    mikrotikUser = mikrotikUser,
                                    mikrotikPassword = mikrotikPassword,
                                    acsUser = acsUser,
                                    acsPassword = acsPassword
                                ))"""
content = content.replace(target_save, replacement_save)

with open(filepath, 'w') as f:
    f.write(content)
