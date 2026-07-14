import re

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "r") as f:
    content = f.read()

launchers = """
    val contactPickerLauncher = rememberLauncherForActivityResult(
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

content = content.replace("    val coroutineScope = rememberCoroutineScope()", "    val coroutineScope = rememberCoroutineScope()\n" + launchers)

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "w") as f:
    f.write(content)

