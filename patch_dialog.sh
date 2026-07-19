#!/bin/bash
sed -i '$d' app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt
cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = cardBg,
            titleContentColor = textMain,
            textContentColor = textSecondary,
            title = { Text("Pelanggan - ${selectedAdmin?.name}") },
            text = {
                if (isLoadingDialog) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = primaryCyan)
                    }
                } else if (dialogPayments.isEmpty()) {
                    Text("Tidak ada data", color = textSecondary)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(dialogPayments) { payment ->
                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Text(payment.customer_name ?: "-", color = textMain, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(payment.created_at ?: "-", color = textSecondary, fontSize = 12.sp)
                                    Text("Rp. ${String.format("%,d", payment.amount?.toLong() ?: 0).replace(",", ".")}", color = successGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(color = cardBgLighter)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Tutup", color = primaryCyan)
                }
            }
        )
    }
}
INNER_EOF
