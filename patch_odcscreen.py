import re

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

# Update state variables
old_states = """        var name by remember { mutableStateOf(editItem?.name ?: "") }
        var location by remember { mutableStateOf(editItem?.location ?: "") }"""

new_states = """        var name by remember { mutableStateOf(editItem?.name ?: "") }
        var location by remember { mutableStateOf(editItem?.location ?: "") }
        var portCount by remember { mutableStateOf(editItem?.portCount?.toString() ?: "") }
        var portInput by remember { mutableStateOf(editItem?.portInput ?: "") }"""
content = content.replace(old_states, new_states)

# Add text fields for new state variables
old_fields = """                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lokasi") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                }
            },"""

new_fields = """                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Lokasi") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = portCount,
                        onValueChange = { portCount = it },
                        label = { Text("Jumlah Port") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                    OutlinedTextField(
                        value = portInput,
                        onValueChange = { portInput = it },
                        label = { Text("Sumber Port Input") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBg,
                            unfocusedBorderColor = cardBorder,
                            focusedTextColor = textMain,
                            unfocusedTextColor = textMain
                        )
                    )
                }
            },"""
content = content.replace(old_fields, new_fields)

# Update confirmButton logic
old_confirm = """                                val newItem = OdcItem(
                                    id = "",
                                    name = name,
                                    location = location
                                )"""

new_confirm = """                                val newItem = OdcItem(
                                    id = "",
                                    name = name,
                                    location = location,
                                    portCount = portCount.toIntOrNull() ?: 0,
                                    portInput = portInput
                                )"""
content = content.replace(old_confirm, new_confirm)

old_edit = """                        odcList = odcList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name, location = location) else it
                        }"""
                        
new_edit = """                        odcList = odcList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name, location = location, portCount = portCount.toIntOrNull() ?: 0, portInput = portInput) else it
                        }"""
content = content.replace(old_edit, new_edit)

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(content)
