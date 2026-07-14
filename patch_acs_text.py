import re

with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "r") as f:
    content = f.read()

target_card_def = """@Composable
fun AcsSummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 8.sp, maxLines = 1)
            Text(value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}"""

rep_card_def = """@Composable
fun AcsSummaryText(title: String, value: String, textColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 8.dp)
    ) {
        Text(title, color = textColor.copy(alpha = 0.8f), fontSize = 10.sp)
        Text(value, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}"""

target_card_usage = """            // Summary Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AcsSummaryCard(
                    title = "Total",
                    value = totalDevices.toString(),
                    icon = Icons.Default.Dns,
                    bgColor = Color(0xFF0055FF),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = false }
                )
                AcsSummaryCard(
                    title = "Online",
                    value = onlineDevices.toString(),
                    icon = Icons.Default.SignalWifi4Bar,
                    bgColor = Color(0xFF008844),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = false }
                )
                AcsSummaryCard(
                    title = "Offline",
                    value = offlineDevices.toString(),
                    icon = Icons.Default.WifiOff,
                    bgColor = Color(0xFFDD3344),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = true }
                )
            }"""

rep_card_usage = """            // Summary Cards
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                AcsSummaryText(
                    title = "Total",
                    value = totalDevices.toString(),
                    textColor = Color(0xFF4488FF),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = false }
                )
                AcsSummaryText(
                    title = "Online",
                    value = onlineDevices.toString(),
                    textColor = Color(0xFF00FF4D),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = false }
                )
                AcsSummaryText(
                    title = "Offline",
                    value = offlineDevices.toString(),
                    textColor = Color(0xFFFF003C),
                    modifier = Modifier.weight(1f),
                    onClick = { showOnlyOffline = true }
                )
            }"""

if target_card_def in content:
    content = content.replace(target_card_def, rep_card_def)
    content = content.replace(target_card_usage, rep_card_usage)
    with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "w") as f:
        f.write(content)
    print("Patched AcsScreen.kt for summary text")
else:
    print("Target not found in AcsScreen.kt")
