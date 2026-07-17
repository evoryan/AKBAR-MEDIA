import re

def patch_file(filename):
    with open(filename, 'r') as f:
        content = f.read()

    port_old = """                    OutlinedTextField(
                        value = portCount,
                        onValueChange = { portCount = it },
                        label = { Text("Jumlah Port") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )"""

    port_new = """                    var portCountExpanded by remember { mutableStateOf(false) }
                    val portOptions = listOf("1:2" to "2", "1:4" to "4", "1:8" to "8", "1:16" to "16")
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = portOptions.find { it.second == portCount }?.first ?: portCount,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Jumlah Port") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { portCountExpanded = !portCountExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Jumlah Port")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { portCountExpanded = true }
                        )
                        DropdownMenu(
                            expanded = portCountExpanded,
                            onDismissRequest = { portCountExpanded = false },
                            modifier = Modifier.background(cardBg).fillMaxWidth(0.9f)
                        ) {
                            portOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.first, color = textMain) },
                                    onClick = {
                                        portCount = option.second
                                        portCountExpanded = false
                                        val rIn = redamanIn.toFloatOrNull()
                                        val rSplitter = when (option.second) {
                                            "2" -> 4.0f
                                            "4" -> 7.2f
                                            "8" -> 10.5f
                                            "16" -> 13.8f
                                            else -> 0.0f
                                        }
                                        if (rIn != null && rSplitter > 0.0f) {
                                            redamanOut = String.format(java.util.Locale.US, "%.2f", rIn - rSplitter)
                                        }
                                    }
                                )
                            }
                        }
                    }"""

    redaman_old = """                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { redamanIn = it },
                        label = { Text("Redaman In") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )"""

    redaman_new = """                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { 
                            redamanIn = it
                            val rIn = it.toFloatOrNull()
                            val rSplitter = when (portCount) {
                                "2" -> 4.0f
                                "4" -> 7.2f
                                "8" -> 10.5f
                                "16" -> 13.8f
                                else -> 0.0f
                            }
                            if (rIn != null && rSplitter > 0.0f) {
                                redamanOut = String.format(java.util.Locale.US, "%.2f", rIn - rSplitter)
                            }
                        },
                        label = { Text("Redaman In") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )"""

    content = content.replace(port_old, port_new)
    content = content.replace(redaman_old, redaman_new)
    
    with open(filename, 'w') as f:
        f.write(content)

patch_file('app/src/main/java/com/example/ui/screens/OdcScreen.kt')
patch_file('app/src/main/java/com/example/ui/screens/OdpScreen.kt')
