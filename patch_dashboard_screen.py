import re
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace the dummy items with LazyColumn? No, Dashboard is inside a verticalScroll, so we can't use LazyColumn directly inside it unless with fixed height. 
# Better just use a forEach.

old_pppoe_box = """                OfflineUserItem("caca_bate", "14:20, 08 Aug")
                Spacer(modifier = Modifier.height(12.dp))
                OfflineUserItem("deni_talun", "09:10, 08 Aug")
                Spacer(modifier = Modifier.height(12.dp))
                OfflineUserItem("eko_bate", "Kemarin")"""

new_pppoe_box = """                if (uiState is DashboardState.Success) {
                    val offlineUsers = (uiState as DashboardState.Success).offlinePppoe
                    if (offlineUsers.isEmpty()) {
                        Text("Tidak ada PPPoE offline", color = textSecondary, fontSize = 14.sp)
                    } else {
                        offlineUsers.take(5).forEach { user ->
                            OfflineUserItem(user.name, user.lastLogoff)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }"""

content = content.replace(old_pppoe_box, new_pppoe_box)
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
