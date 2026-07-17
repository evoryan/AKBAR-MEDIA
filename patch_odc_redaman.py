import re

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

# Add mutable states
states = """        var portInput by remember { mutableStateOf(editItem?.portInput ?: "") }
        var redamanIn by remember { mutableStateOf(editItem?.redamanIn ?: "") }
        var redamanOut by remember { mutableStateOf(editItem?.redamanOut ?: "") }"""
content = content.replace("        var portInput by remember { mutableStateOf(editItem?.portInput ?: \"\") }", states)

# Add text fields
fields = """                    }
                    OutlinedTextField(
                        value = redamanIn,
                        onValueChange = { redamanIn = it },
                        label = { Text("Redaman In") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = redamanOut,
                        onValueChange = { redamanOut = it },
                        label = { Text("Redaman Out") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg, unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain, unfocusedTextColor = textMain
                        )
                    )
                }"""
content = content.replace("                    }\n                }", fields)

# Update initialization
init_new = """                                    portCount = portCount.toIntOrNull() ?: 0,
                                    portInput = portInput,
                                    redamanIn = redamanIn,
                                    redamanOut = redamanOut
                                )"""
content = content.replace("                                    portCount = portCount.toIntOrNull() ?: 0,\n                                    portInput = portInput\n                                )", init_new)

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(content)
