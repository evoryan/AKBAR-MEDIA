import re

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    content = f.read()

# Update state variables
old_states = """        var name by remember { mutableStateOf(editItem?.name ?: "") }
        var portCount by remember { mutableStateOf(editItem?.portCount?.toString() ?: "") }
        var selectedOdc by remember { mutableStateOf(editItem?.odcId ?: odcList.firstOrNull()?.id ?: "") }
        var expanded by remember { mutableStateOf(false) }"""

new_states = """        var name by remember { mutableStateOf(editItem?.name ?: "") }
        var portCount by remember { mutableStateOf(editItem?.portCount?.toString() ?: "") }
        var portInput by remember { mutableStateOf(editItem?.portInput ?: "") }
        var selectedOdc by remember { mutableStateOf(editItem?.odcId ?: odcList.firstOrNull()?.id ?: "") }
        var expanded by remember { mutableStateOf(false) }"""
content = content.replace(old_states, new_states)

# Add text fields for new state variables
old_fields = """                    OutlinedTextField(
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
                    // ODC Dropdown"""

new_fields = """                    OutlinedTextField(
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
                    // ODC Dropdown"""
content = content.replace(old_fields, new_fields)

# Update confirmButton logic
old_confirm = """                                val newItem = OdpItem(
                                    id = "",
                                    name = name,
                                    odcId = selectedOdc,
                                    portCount = port
                                )"""

new_confirm = """                                val newItem = OdpItem(
                                    id = "",
                                    name = name,
                                    odcId = selectedOdc,
                                    portCount = port,
                                    portInput = portInput
                                )"""
content = content.replace(old_confirm, new_confirm)

old_edit = """                        odpList = odpList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name, odcId = selectedOdc, portCount = port) else it
                        }"""
                        
new_edit = """                        odpList = odpList.map {
                            if (it.id == (editItem?.id ?: "")) it.copy(name = name, odcId = selectedOdc, portCount = port, portInput = portInput) else it
                        }"""
content = content.replace(old_edit, new_edit)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(content)
