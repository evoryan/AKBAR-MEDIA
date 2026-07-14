import re

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "r") as f:
    content = f.read()

# Remove combinedClickable
target_combined = """            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            )"""
content = content.replace(target_combined, "")

# Replace the bottom row of buttons
target_buttons = """                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (customer.status != "LUNAS CASH") {
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                        }
                    }
                }
                Button(
                    onClick = {
                        if (customer.status != "LUNAS CASH") {
                            onPayClick()
                        } else {
                            onDetailClick()
                        }
                    },
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (customer.status != "LUNAS CASH") neonCyan else Color.Transparent, contentColor = if (customer.status != "LUNAS CASH") Color.Black else neonCyan),
                    border = if (customer.status == "LUNAS CASH") androidx.compose.foundation.BorderStroke(1.dp, neonCyan) else null,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (customer.status != "LUNAS CASH") "BAYAR" else "DETAIL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }"""

rep_buttons = """                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (customer.status != "LUNAS CASH") {
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF003C))
                        }
                    } else {
                        Button(
                            onClick = onLongPress,
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color(0xFFFF003C)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF003C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BATAL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = {
                            if (customer.status != "LUNAS CASH") {
                                onPayClick()
                            } else {
                                onDetailClick()
                            }
                        },
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (customer.status != "LUNAS CASH") neonCyan else Color.Transparent, contentColor = if (customer.status != "LUNAS CASH") Color.Black else neonCyan),
                        border = if (customer.status == "LUNAS CASH") androidx.compose.foundation.BorderStroke(1.dp, neonCyan) else null,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (customer.status != "LUNAS CASH") "BAYAR" else "DETAIL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }"""

content = content.replace(target_buttons, rep_buttons)

with open("app/src/main/java/com/example/ui/screens/BillingScreen.kt", "w") as f:
    f.write(content)
