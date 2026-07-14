import re

# Fix BillingScreen.kt
with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    billing = f.read()
billing = billing.replace("@Composable\n@OptIn(ExperimentalFoundationApi::class)\n@Composable", "@OptIn(ExperimentalFoundationApi::class)\n@Composable")
with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(billing)


# Fix AddCustomerScreen.kt
with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "r") as f:
    addc = f.read()

# We just need to move permissionLauncher down below contactPickerLauncher.
# They are both val declarations inside the Composable.
# Let's extract permissionLauncher and put it after contactPickerLauncher.
pattern_permission = r"(\s*val permissionLauncher = rememberLauncherForActivityResult\([\s\S]*?\}\n\s*\})"
match_perm = re.search(pattern_permission, addc)
if match_perm:
    perm_block = match_perm.group(1)
    addc = addc.replace(perm_block, "")
    
    # Find contactPickerLauncher end
    pattern_contact = r"(\s*val contactPickerLauncher = rememberLauncherForActivityResult\([\s\S]*?\}\n\s*\})"
    match_contact = re.search(pattern_contact, addc)
    if match_contact:
        contact_block = match_contact.group(1)
        addc = addc.replace(contact_block, contact_block + "\n" + perm_block)

with open("app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt", "w") as f:
    f.write(addc)

