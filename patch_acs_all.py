import re

# 1. Update AcsScreen.kt to make summary cards smaller
with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "r") as f:
    content = f.read()

target_card = """fun AcsSummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
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

rep_card = """fun AcsSummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
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

if target_card in content:
    content = content.replace(target_card, rep_card)
    with open("app/src/main/java/com/example/ui/screens/AcsScreen.kt", "w") as f:
        f.write(content)
    print("Patched AcsSummaryCard")
else:
    print("AcsSummaryCard target not found")


# 2. Update VPS/server.js for PPPoE User
with open("VPS/server.js", "r") as f:
    server_content = f.read()

target_pppoe = """                        const pppoeUser = (() => {
                            for (let i = 1; i <= 5; i++) {
                                for (let j = 1; j <= 3; j++) {
                                    let u = getVal(`InternetGatewayDevice.WANDevice.1.WANConnectionDevice.${i}.WANPPPConnection.${j}.Username`);
                                    if (u) return u;
                                }
                            }
                            return getVal('InternetGatewayDevice.WANDevice.1.WANPPPConnection.1.Username') ||
                                   getVal('Device.WANDevice.1.WANPPPConnection.1.Username') ||
                                   getVal('Device.Users.User.1.Username') ||
                                   (d.summary && d.summary.username) ||
                                   (d.summary && d.summary.mac) ||
                                   'Unknown';
                        })();"""

rep_pppoe = """                        const pppoeUser = (() => {
                            for (let dev = 1; dev <= 2; dev++) {
                                for (let i = 1; i <= 10; i++) {
                                    for (let j = 1; j <= 5; j++) {
                                        let u = getVal(`InternetGatewayDevice.WANDevice.${dev}.WANConnectionDevice.${i}.WANPPPConnection.${j}.Username`) ||
                                                getVal(`Device.WANDevice.${dev}.WANConnectionDevice.${i}.WANPPPConnection.${j}.Username`);
                                        if (u && String(u).trim() !== '') return u;
                                    }
                                }
                            }
                            return getVal('InternetGatewayDevice.WANDevice.1.WANPPPConnection.1.Username') ||
                                   getVal('Device.WANDevice.1.WANPPPConnection.1.Username') ||
                                   getVal('Device.PPP.Interface.1.Username') ||
                                   getVal('Device.Users.User.1.Username') ||
                                   (d.summary && d.summary.username) ||
                                   (d.summary && d.summary.mac) ||
                                   'Unknown';
                        })();"""

if target_pppoe in server_content:
    server_content = server_content.replace(target_pppoe, rep_pppoe)
    with open("VPS/server.js", "w") as f:
        f.write(server_content)
    print("Patched pppoeUser")
else:
    print("pppoeUser target not found")

