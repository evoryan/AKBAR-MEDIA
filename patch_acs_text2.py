import re

with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("AcsSummaryCard(", "AcsSummaryText(")
content = content.replace("bgColor =", "textColor =")
content = content.replace("icon = Icons.Default.Dns,", "")
content = content.replace("icon = Icons.Default.SignalWifi4Bar,", "")
content = content.replace("icon = Icons.Default.WifiOff,", "")

with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "w") as f:
    f.write(content)
