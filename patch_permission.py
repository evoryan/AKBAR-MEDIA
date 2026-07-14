import re

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "r") as f:
    content = f.read()

target = """            val contactPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickContact(),"""

rep = """            val contactPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickContact(),"""

permission_logic = """
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Izin membaca kontak ditolak", Toast.LENGTH_SHORT).show()
        }
    }
"""

content = content.replace("    var isPhoneError by remember { mutableStateOf(false) }", "    var isPhoneError by remember { mutableStateOf(false) }\n" + permission_logic)

content = content.replace("IconButton(onClick = { contactPickerLauncher.launch(null) })", "IconButton(onClick = { permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS) })")

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "w") as f:
    f.write(content)

