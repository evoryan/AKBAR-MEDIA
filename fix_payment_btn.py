import re

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'r') as f:
    content = f.read()

old_btn = """            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFFF), contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("BAYAR SEKARANG", fontWeight = FontWeight.Bold)
            }"""

new_btn = """            val isPaid = customer?.status?.contains("LUNAS", ignoreCase = true) ?: false
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isPaid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF), 
                    contentColor = Color.Black,
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.LightGray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isPaid) "SUDAH LUNAS" else "BAYAR SEKARANG", fontWeight = FontWeight.Bold)
            }"""

content = content.replace(old_btn, new_btn)

with open('app/src/main/java/com/example/ui/screens/PaymentScreen.kt', 'w') as f:
    f.write(content)
