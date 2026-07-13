import re

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

old_display = """                                    Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.location, color = textSecondary, fontSize = 14.sp)"""

new_display = """                                    Text(item.name, color = textMain, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.location, color = textSecondary, fontSize = 14.sp)
                                    if (item.portCount > 0) {
                                        Text("Port: ${item.portCount} | Input: ${item.portInput}", color = primaryBg, fontSize = 12.sp)
                                    }"""

content = content.replace(old_display, new_display)

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(content)
