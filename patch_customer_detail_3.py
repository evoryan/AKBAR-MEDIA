import re

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'r') as f:
    content = f.read()

old_actions = """                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = textMain)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Image, contentDescription = "Image", tint = textMain)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = textMain)
                    }
                },"""

new_actions = """                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Fitur edit pelanggan akan segera hadir", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = textMain)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Image, contentDescription = "Image", tint = textMain)
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR Code", tint = textMain)
                    }
                },"""

content = content.replace(old_actions, new_actions)

with open('app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt', 'w') as f:
    f.write(content)
