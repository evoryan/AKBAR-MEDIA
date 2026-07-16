import re

with open("app/src/main/java/com/example/ui/data/remote/NetworkMonitor.kt", "r") as f:
    content = f.read()

content = content.replace('Toast.makeText(context.applicationContext, "Jaringan kembali normal. Mode Online aktif.", Toast.LENGTH_LONG).show()', 'Toast.makeText(context.applicationContext, "Jaringan kembali normal.", Toast.LENGTH_LONG).show()')
content = content.replace('Toast.makeText(context.applicationContext, "Koneksi terputus. Beralih ke Mode Offline.", Toast.LENGTH_LONG).show()', 'Toast.makeText(context.applicationContext, "Koneksi terputus.", Toast.LENGTH_LONG).show()')
content = content.replace('Toast.makeText(context.applicationContext, "Tidak ada jaringan. Beralih ke Mode Offline.", Toast.LENGTH_LONG).show()', 'Toast.makeText(context.applicationContext, "Tidak ada jaringan.", Toast.LENGTH_LONG).show()')

with open("app/src/main/java/com/example/ui/data/remote/NetworkMonitor.kt", "w") as f:
    f.write(content)

