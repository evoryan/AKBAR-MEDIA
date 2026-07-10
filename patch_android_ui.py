import re

with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'r') as f:
    content = f.read()

# Update AcsDeviceItem to show status
old_block = """                Column(modifier = Modifier.weight(0.45f)) {
                    Text(device.username, color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    if (device.areaName.isNotEmpty()) {
                        Text(device.areaName, color = primaryCyan, fontSize = 10.sp)
                    }
                }"""

new_block = """                Column(modifier = Modifier.weight(0.45f)) {
                    Text(device.username, color = textMain, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (device.isOnline) successGreen else errorRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (device.isOnline) "Online" else "Offline",
                            color = if (device.isOnline) successGreen else errorRed,
                            fontSize = 10.sp
                        )
                        if (device.areaName.isNotEmpty()) {
                            Text(" • ${device.areaName}", color = primaryCyan, fontSize = 10.sp)
                        }
                    }
                }"""

content = content.replace(old_block, new_block)

# Since we need successGreen in AcsDeviceItem, let's pass it
if "successGreen: Color" not in content:
    content = content.replace("    errorRed: Color\n) {", "    errorRed: Color,\n    successGreen: Color\n) {")
    content = content.replace("errorRed = errorRed\n                    )", "errorRed = errorRed,\n                        successGreen = successGreen\n                    )")

with open('app/src/main/java/com/example/ui/screens/AcsScreen.kt', 'w') as f:
    f.write(content)
