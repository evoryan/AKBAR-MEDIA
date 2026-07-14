import re

with open("app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt", "r") as f:
    content = f.read()

target = """                // Top Section (Profile)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(primaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            customer?.name?.take(1)?.uppercase() ?: "P",
                            color = neonCyan,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(customer?.name ?: "Nama Pelanggan", color = textMain, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(customer?.phone ?: "-", color = textSecondary, fontSize = 14.sp)
                }"""

rep = """                // Top Section (Profile)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(headerBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(primaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            customer?.name?.take(1)?.uppercase() ?: "P",
                            color = neonCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(customer?.name ?: "Nama Pelanggan", color = textMain, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(customer?.phone ?: "-", color = textSecondary, fontSize = 12.sp)
                }"""

if target in content:
    content = content.replace(target, rep)
    with open("app/src/main/java/com/example/ui/screens/CustomerDetailScreen.kt", "w") as f:
        f.write(content)
    print("Patched CustomerDetailScreen.kt header")
else:
    print("Target not found in CustomerDetailScreen.kt")
