import re

with open("app/src/main/java/com/example/ui/screens/OdcScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.ArrowDropDown" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Add", "import androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.ArrowDropDown")

pattern = r"""                    OutlinedTextField\(
                        value = portInput,
                        onValueChange = \{ portInput = it \},
                        label = \{ Text\("Sumber Port Input"\) \},
                        colors = OutlinedTextFieldDefaults\.colors\(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        \)
                    \)"""

replacement = """                    var portInputExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it },
                            label = { Text("Sumber Port Input") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryBg,
                                unfocusedBorderColor = cardBorder,
                                focusedTextColor = textMain,
                                unfocusedTextColor = textMain
                            ),
                            trailingIcon = {
                                IconButton(onClick = { portInputExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih ODC")
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = portInputExpanded,
                            onDismissRequest = { portInputExpanded = false },
                            modifier = Modifier.background(cardBg)
                        ) {
                            odcList.forEach { odc ->
                                DropdownMenuItem(
                                    text = { Text(odc.name, color = textMain) },
                                    onClick = {
                                        portInput = odc.name
                                        portInputExpanded = false
                                    }
                                )
                            }
                        }
                    }"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/ui/screens/OdcScreen.kt", "w") as f:
    f.write(content)

