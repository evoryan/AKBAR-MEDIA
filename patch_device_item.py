with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'r') as f:
    content = f.read()

old_code = """                Text(device.username, color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.45f))"""
new_code = """                Column(modifier = Modifier.weight(0.45f)) {
                    Text(device.username, color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    if (device.areaName.isNotEmpty()) {
                        Text(device.areaName, color = primaryCyan, fontSize = 10.sp)
                    }
                }"""

content = content.replace(old_code, new_code)

with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'w') as f:
    f.write(content)
