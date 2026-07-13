import re

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    content = f.read()

old_save = """                    val port = portCount.toIntOrNull() ?: 0
                    if (editItem == null) {"""

new_save = """                    val port = portCount.toIntOrNull() ?: 0
                    if (selectedOdc.isEmpty()) {
                        Toast.makeText(context, "Silakan pilih ODC terlebih dahulu", Toast.LENGTH_SHORT).show()
                        return@TextButton
                    }
                    if (editItem == null) {"""
                    
content = content.replace(old_save, new_save)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(content)
