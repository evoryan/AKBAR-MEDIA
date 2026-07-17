import re

with open('app/src/main/java/com/example/ui/screens/RasioScreen.kt', 'r') as f:
    content = f.read()

ukuran_old = """                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Ukuran") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )"""

ukuran_new = """                    var sizeExpanded by remember { mutableStateOf(false) }
                    val sizeOptions = listOf(
                        "01:99" to Pair(20.0f, 0.05f),
                        "02:98" to Pair(17.0f, 0.10f),
                        "03:97" to Pair(15.2f, 0.13f),
                        "04:96" to Pair(14.0f, 0.18f),
                        "05:95" to Pair(13.0f, 0.22f),
                        "06:94" to Pair(12.2f, 0.27f),
                        "07:93" to Pair(11.5f, 0.32f),
                        "08:92" to Pair(11.0f, 0.36f),
                        "09:91" to Pair(10.5f, 0.41f),
                        "10:90" to Pair(10.0f, 0.46f),
                        "15:85" to Pair(8.2f, 0.71f),
                        "20:80" to Pair(7.0f, 0.97f),
                        "25:75" to Pair(6.0f, 1.25f),
                        "30:70" to Pair(5.2f, 1.55f),
                        "35:65" to Pair(4.6f, 1.87f),
                        "40:60" to Pair(4.0f, 2.22f),
                        "45:55" to Pair(3.5f, 2.60f),
                        "50:50" to Pair(3.0f, 3.00f)
                    )
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = size,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Ukuran") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { sizeExpanded = !sizeExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Ukuran")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { sizeExpanded = true }
                        )
                        DropdownMenu(
                            expanded = sizeExpanded,
                            onDismissRequest = { sizeExpanded = false },
                            modifier = Modifier.background(cardBg).fillMaxWidth(0.9f)
                        ) {
                            sizeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.first, color = textMain) },
                                    onClick = {
                                        size = option.first
                                        sizeExpanded = false
                                        val rIn = redamanIn.toFloatOrNull()
                                        if (rIn != null) {
                                            redamanOutA = String.format(java.util.Locale.US, "%.2f", rIn - option.second.first)
                                            redamanOutB = String.format(java.util.Locale.US, "%.2f", rIn - option.second.second)
                                        }
                                    }
                                )
                            }
                        }
                    }"""

redaman_in_old = """                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { redamanIn = it },
                        label = { Text("Redaman Input") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )"""

redaman_in_new = """                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { 
                            redamanIn = it 
                            val rIn = it.toFloatOrNull()
                            if (rIn != null) {
                                val selectedOption = sizeOptions.find { it.first == size }
                                if (selectedOption != null) {
                                    redamanOutA = String.format(java.util.Locale.US, "%.2f", rIn - selectedOption.second.first)
                                    redamanOutB = String.format(java.util.Locale.US, "%.2f", rIn - selectedOption.second.second)
                                }
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        label = { Text("Redaman Input") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )"""

import_old = "import androidx.compose.material.icons.filled.Add"
import_new = "import androidx.compose.foundation.clickable\nimport androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.ArrowDropDown"

if 'import androidx.compose.foundation.clickable' not in content:
    content = content.replace(import_old, import_new)
content = content.replace(ukuran_old, ukuran_new)
content = content.replace(redaman_in_old, redaman_in_new)

with open('app/src/main/java/com/example/ui/screens/RasioScreen.kt', 'w') as f:
    f.write(content)
