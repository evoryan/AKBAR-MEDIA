import re

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    content = f.read()

old_display = """                                    Text("ODC: ${odc?.name ?: "Unknown"}", color = textSecondary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Port: ${item.portCount}", color = primaryBg, fontSize = 14.sp)"""

new_display = """                                    Text("ODC: ${odc?.name ?: "Unknown"}", color = textSecondary, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Port: ${item.portCount} | Input: ${item.portInput}", color = primaryBg, fontSize = 14.sp)"""

content = content.replace(old_display, new_display)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(content)
