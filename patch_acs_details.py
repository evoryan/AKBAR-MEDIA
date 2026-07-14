import re

with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "r") as f:
    content = f.read()

target = """                    AcsDetailRow("SSID", device.ssid, textMain, textSecondary)
                    AcsDetailRow("User Konek", device.connectedUsers.toString(), textMain, textSecondary)
                    AcsDetailRow("Nomor Pelanggan", device.customerNumber, textMain, textSecondary)
                    AcsDetailRow("Redaman", device.rxPower, textMain, textSecondary)"""

rep = """                    AcsDetailRow("SSID", device.ssid, textMain, textSecondary)
                    AcsDetailRow("Password", device.wifiPassword, textMain, textSecondary)
                    AcsDetailRow("User Konek", device.connectedUsers.toString(), textMain, textSecondary)
                    AcsDetailRow("Nomor Pelanggan", device.customerNumber, textMain, textSecondary)
                    AcsDetailRow("Redaman", if (device.rxPower != "-" && device.rxPower.isNotEmpty()) "${device.rxPower} dBm" else "-", textMain, textSecondary)"""

if target in content:
    content = content.replace(target, rep)
    with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "w") as f:
        f.write(content)
    print("Patched AcsDetailRow block")
else:
    print("Target AcsDetailRow block not found")

