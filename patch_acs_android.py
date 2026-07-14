import re

# 1. Update ApiService.kt
with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = """data class AcsDevice(
    val id: String,
    val username: String,
    val isOnline: Boolean,
    val ssid: String = "ELS_A",
    val connectedUsers: Int = 0,
    val customerNumber: String = "-",
    val rxPower: String = "-23.27",
    val areaName: String = ""
)"""

rep = """data class AcsDevice(
    val id: String,
    val username: String,
    val isOnline: Boolean,
    val ssid: String = "Unknown",
    val wifiPassword: String = "-",
    val connectedUsers: Int = 0,
    val customerNumber: String = "-",
    val rxPower: String = "-",
    val areaName: String = ""
)"""

if target in content:
    content = content.replace(target, rep)
else:
    print("ApiService target not found")

with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
    f.write(content)


# 2. Update AcsScreen.kt
with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "r") as f:
    content2 = f.read()

if target in content2:
    content2 = content2.replace(target, rep)
else:
    print("AcsScreen AcsDevice target not found")

target_summary = """fun AcsSummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, maxLines = 1)
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}"""

rep_summary = """fun AcsSummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 9.sp, maxLines = 1)
            Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}"""

if target_summary in content2:
    content2 = content2.replace(target_summary, rep_summary)
else:
    print("AcsSummaryCard target not found")

target_details = """                    AcsDetailRow("Customer No.", device.customerNumber, textMain, textSecondary)
                    AcsDetailRow("User Koneksi", device.connectedUsers.toString(), textMain, textSecondary)
                    AcsDetailRow("SSID", device.ssid, textMain, textSecondary)
                    AcsDetailRow("Redaman", "${device.rxPower} dBm", textMain, textSecondary)"""

rep_details = """                    AcsDetailRow("Customer No.", device.customerNumber, textMain, textSecondary)
                    AcsDetailRow("User Koneksi", device.connectedUsers.toString(), textMain, textSecondary)
                    AcsDetailRow("SSID", device.ssid, textMain, textSecondary)
                    AcsDetailRow("Password", device.wifiPassword, textMain, textSecondary)
                    AcsDetailRow("Redaman", if (device.rxPower != "-" && device.rxPower.isNotEmpty()) "${device.rxPower} dBm" else "-", textMain, textSecondary)"""

if target_details in content2:
    content2 = content2.replace(target_details, rep_details)
else:
    print("AcsDetailRow target not found")

with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "w") as f:
    f.write(content2)

print("Android patched")
