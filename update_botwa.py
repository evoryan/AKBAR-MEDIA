import re

filepath = 'app/src/main/java/com/example/ui/screens/BotWaScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Wrap the OutlinedButton for Hapus Session
target = """OutlinedButton(
                            onClick = { /* hapus session */ },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC3545))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus Session")
                        }"""
replacement = """if (currentUser?.role == UserRole.SUPER_ADMIN) {
                        OutlinedButton(
                            onClick = { /* hapus session */ },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC3545))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus Session")
                        }
                        }"""
content = content.replace(target, replacement)

with open(filepath, 'w') as f:
    f.write(content)

