import re

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "r") as f:
    content = f.read()

target = """                Button(
                    onClick = { showSetorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Setor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }"""

rep = """                Button(
                    onClick = { showSetorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Setor", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "w") as f:
    f.write(content)
