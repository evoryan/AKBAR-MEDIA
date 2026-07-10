import re

with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'r') as f:
    content = f.read()

# 1. Update AcsDevice data class
old_acs_device = """data class AcsDevice(
    val id: String,
    val username: String,
    val isOnline: Boolean,
    val ssid: String = "ELS_A",
    val connectedUsers: Int = 0,
    val customerNumber: String = "-",
    val rxPower: String = "-23.27"
)"""

new_acs_device = """data class AcsDevice(
    val id: String,
    val username: String,
    val isOnline: Boolean,
    val ssid: String = "ELS_A",
    val connectedUsers: Int = 0,
    val customerNumber: String = "-",
    val rxPower: String = "-23.27",
    val areaName: String = ""
)"""
content = content.replace(old_acs_device, new_acs_device)

# 2. Update state variables and initialization
old_state_init = """    var searchQuery by remember { mutableStateOf("") }
    var showOnlyOffline by remember { mutableStateOf(false) }

    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }
    var selectedArea by remember { mutableStateOf<Area?>(null) }
    var expandedAreaDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getAreas()
            areas = res.filter { it.apiDomain != null && it.apiDomain.isNotEmpty() }
            if (areas.isNotEmpty()) {
                selectedArea = areas.first()
            }
        } catch(e: Exception) {
        }
    }
        val allDevices = remember {
        listOf(
            AcsDevice("1", "turifah", true, "ELS_A", 0),
            AcsDevice("2", "harti", true, "Harti_WIFI", 3, "-", "-21.40"),
            AcsDevice("3", "bila", true, "Bila_WIFI", 2),
            AcsDevice("4", "dendi", false),
            AcsDevice("5", "dinda", true),
            AcsDevice("6", "fadil_gd", true),
            AcsDevice("7", "rabo", true),
            AcsDevice("8", "miftah", false),
            AcsDevice("9", "anwar", true),
            AcsDevice("10", "yuli", true)
        )
    }"""

new_state_init = """    var searchQuery by remember { mutableStateOf("") }
    var showOnlyOffline by remember { mutableStateOf(false) }
    
    var allDevices by remember { mutableStateOf<List<AcsDevice>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            allDevices = ApiClient.apiService.getAcsDevices()
        } catch(e: Exception) {
            errorMsg = "Gagal memuat data ACS: ${e.message}"
        } finally {
            isLoading = false
        }
    }"""

# Try to replace the old state init. Wait, the old_state_init might have slight spacing differences.
# Let's use regex instead.

state_regex = re.compile(r'    var searchQuery by remember \{ mutableStateOf\(""\) \}.*?    val displayedDevices', re.DOTALL)
content = state_regex.sub(new_state_init + "\n\n    val displayedDevices", content)

# 3. Remove the dropdown UI
dropdown_regex = re.compile(r'            // Area Selector.*?// Summary Cards', re.DOTALL)
content = dropdown_regex.sub('            // Summary Cards', content)

# 4. Modify the device actions to call API
# In AcsScreen, we have:
#                            onConfirm = { newSsid ->
#                                isUpdating = true
#                                updateResult = null
#                                coroutineScope.launch {
#                                    delay(2000)
#                                    isUpdating = false
#                                    updateResult = "Ubah SSID berhasil!"
#                                }
#                            }

ssid_action = re.compile(r'onConfirm = \{ newSsid ->\s+isUpdating = true\s+updateResult = null\s+coroutineScope\.launch \{\s+delay\(2000\)\s+isUpdating = false\s+updateResult = "Ubah SSID berhasil!"\s+\}\s+\}')
content = ssid_action.sub("""onConfirm = { newSsid ->
                isUpdating = true
                updateResult = null
                coroutineScope.launch {
                    try {
                        val res = ApiClient.apiService.acsAction(device.id, mapOf("action" to "set_ssid", "value" to newSsid))
                        updateResult = res.message
                    } catch(e: Exception) {
                        updateResult = "Gagal: ${e.message}"
                    } finally {
                        isUpdating = false
                    }
                }
            }""", content)

pass_action = re.compile(r'onConfirm = \{ newPass ->\s+if \(newPass\.length < 8\) \{\s+updateResult = "Gagal: Password minimal 8 karakter!"\s+\} else \{\s+isUpdating = true\s+updateResult = null\s+coroutineScope\.launch \{\s+delay\(2000\)\s+isUpdating = false\s+updateResult = "Ubah password berhasil!"\s+\}\s+\}\s+\}')
content = pass_action.sub("""onConfirm = { newPass ->
                if (newPass.length < 8) {
                    updateResult = "Gagal: Password minimal 8 karakter!"
                } else {
                    isUpdating = true
                    updateResult = null
                    coroutineScope.launch {
                        try {
                            val res = ApiClient.apiService.acsAction(device.id, mapOf("action" to "set_password", "value" to newPass))
                            updateResult = res.message
                        } catch(e: Exception) {
                            updateResult = "Gagal: ${e.message}"
                        } finally {
                            isUpdating = false
                        }
                    }
                }
            }""", content)

restart_action = re.compile(r'onConfirm = \{\s+isUpdating = true\s+updateResult = null\s+coroutineScope\.launch \{\s+delay\(2000\)\s+isUpdating = false\s+updateResult = "Restart perangkat berhasil!"\s+\}\s+\}')
content = restart_action.sub("""onConfirm = { 
                isUpdating = true
                updateResult = null
                coroutineScope.launch {
                    try {
                        val res = ApiClient.apiService.acsAction(device.id, mapOf("action" to "reboot"))
                        updateResult = res.message
                    } catch(e: Exception) {
                        updateResult = "Gagal: ${e.message}"
                    } finally {
                        isUpdating = false
                    }
                }
            }""", content)

# 5. Show loading or error
# After Area Selector removal, we have `// Summary Cards`
content = content.replace("            // Summary Cards", """            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryCyan)
                }
                return@Scaffold
            }
            
            if (errorMsg != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMsg!!, color = errorRed, fontSize = 16.sp)
                }
                return@Scaffold
            }

            // Summary Cards""")


with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'w') as f:
    f.write(content)
