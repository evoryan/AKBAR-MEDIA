import re

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "r") as f:
    content = f.read()

target_import = "import androidx.compose.material3.*"
rep_import = "import androidx.compose.material3.*\nimport androidx.activity.compose.rememberLauncherForActivityResult\nimport androidx.activity.result.contract.ActivityResultContracts\nimport android.provider.ContactsContract\nimport androidx.compose.material.icons.filled.Contacts"

target_phone = """            OutlinedTextField(
                value = phone,
                onValueChange = { 
                    val newPhone = it.filter { char -> char.isDigit() }
                    phone = newPhone
                    isPhoneError = false
                },
                label = { Text("Nomor HP (WhatsApp)", color = textSecondary) },
                placeholder = { Text("08123456789", color = textSecondary.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                isError = isPhoneError,"""

rep_phone = """            val contactPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickContact(),
                onResult = { uri ->
                    if (uri != null) {
                        val cursor = context.contentResolver.query(uri, null, null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                            val hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                            if (idIndex != -1 && hasPhoneIndex != -1) {
                                val id = cursor.getString(idIndex)
                                val hasPhone = cursor.getString(hasPhoneIndex)
                                if (hasPhone.toInt() > 0) {
                                    val phones = context.contentResolver.query(
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        arrayOf(id),
                                        null
                                    )
                                    if (phones != null && phones.moveToFirst()) {
                                        val numIndex = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                        if (numIndex != -1) {
                                            var num = phones.getString(numIndex)
                                            num = num.replace(Regex("[^0-9]"), "")
                                            if (num.startsWith("62")) {
                                                num = "0" + num.substring(2)
                                            }
                                            phone = num
                                            isPhoneError = false
                                        }
                                        phones.close()
                                    }
                                }
                            }
                            cursor.close()
                        }
                    }
                }
            )
            
            OutlinedTextField(
                value = phone,
                onValueChange = { 
                    val newPhone = it.filter { char -> char.isDigit() }
                    phone = newPhone
                    isPhoneError = false
                },
                label = { Text("Nomor HP (WhatsApp)", color = textSecondary) },
                placeholder = { Text("08123456789", color = textSecondary.copy(alpha = 0.5f)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                        Icon(Icons.Default.Contacts, contentDescription = "Pilih Kontak", tint = neonCyan)
                    }
                },
                isError = isPhoneError,"""

if "import androidx.activity.compose.rememberLauncherForActivityResult" not in content:
    content = content.replace(target_import, rep_import)
    content = content.replace(target_phone, rep_phone)
    with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "w") as f:
        f.write(content)
    print("Patched AddCustomerScreen.kt")
else:
    print("Already patched")
