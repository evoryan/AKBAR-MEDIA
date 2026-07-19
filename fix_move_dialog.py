with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'r') as f:
    content = f.read()

# The dialog code starts with `if (showDialog) {` and ends at the end of the file
dialog_start_idx = content.rfind('if (showDialog) {')
dialog_code = content[dialog_start_idx:]
# The dialog code also has a closing brace `}` that was meant for `UangDiAdminScreen` but closed `AdminItemCard` instead!

# We need to remove it from the end
content = content[:dialog_start_idx]

# We need to add a closing brace to AdminItemCard since we took it away
content += '}\n'

# Find the end of UangDiAdminScreen (line 157-ish, where `} // Scaffold` is, wait it's just `}`)
# Let's find the closing brace before `@Composable\nfun AdminItemCard(`
admin_item_idx = content.find('@Composable\nfun AdminItemCard(')
before_admin_item = content[:admin_item_idx]
after_admin_item = content[admin_item_idx:]

# The last closing brace in before_admin_item belongs to UangDiAdminScreen
last_brace_idx = before_admin_item.rfind('}')
new_before = before_admin_item[:last_brace_idx] + dialog_code[:-2] + '}\n\n'

with open('app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt', 'w') as f:
    f.write(new_before + after_admin_item)
