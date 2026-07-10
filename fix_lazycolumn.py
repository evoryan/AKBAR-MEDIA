with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

block = """        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = Color(0xFF11111A),
                title = { Text("Input Nominal ($selectedCategory)", color = textMain, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            label = { Text("Nominal (Rp)", color = textSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = inputDescription,
                            onValueChange = { inputDescription = it },
                            label = { Text("Keterangan", color = textSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryCyan, unfocusedBorderColor = textSecondary,
                                focusedTextColor = textMain, unfocusedTextColor = textMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val amount = inputAmount.toDoubleOrNull() ?: 0.0
                                    ApiClient.apiService.addPembukuan(PembukuanRequest(selectedType, selectedCategory, amount, inputDescription))
                                    fetchData()
                                    showAddDialog = false
                                } catch (e: Exception) {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryCyan, contentColor = Color.Black)
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Batal", color = textMain)
                    }
                },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }"""

if block in content:
    content = content.replace(block + "\n\n", "")
    content = content.replace(block + "\n", "")
    content = content.replace(block, "")

# Now inject it BEFORE the last `}` of Scaffold
# The structure is:
#         } // end LazyColumn
#     } // end Scaffold inner padding
# } // end PembukuanScreen
# 
# @Composable
# fun PembukuanItem

insert_target = """        }
    }
}

@Composable
fun PembukuanItem"""

if insert_target in content:
    content = content.replace(insert_target, "        }\n" + block + "\n    }\n}\n\n@Composable\nfun PembukuanItem")

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)
