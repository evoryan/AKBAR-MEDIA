import sys

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if line.strip() == "// Dropdown Area Filter":
        skip = True
        new_lines.append('''            // Filter and Search Row
            var expanded by remember { mutableStateOf(false) }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedArea,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Area", color = textMain.copy(alpha = 0.7f), fontSize = 12.sp) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonCyan, unfocusedBorderColor = textMain.copy(alpha = 0.5f),
                                focusedTextColor = textMain, unfocusedTextColor = textMain,
                                focusedTrailingIconColor = neonCyan, unfocusedTrailingIconColor = textMain.copy(alpha = 0.5f),
                                focusedLabelColor = neonCyan, unfocusedLabelColor = textMain.copy(alpha = 0.7f)
                            )
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = cardBg) {
                            areas.forEach { area ->
                                DropdownMenuItem(
                                    text = { Text(area, color = if (selectedArea == area) neonCyan else textMain, fontSize = 14.sp) },
                                    onClick = { selectedArea = area; expanded = false }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cari", color = textMain.copy(alpha = 0.7f), fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = neonCyan, unfocusedBorderColor = textMain.copy(alpha = 0.5f),
                        focusedTextColor = textMain, unfocusedTextColor = textMain,
                        focusedLabelColor = neonCyan, unfocusedLabelColor = textMain.copy(alpha = 0.7f)
                    ),
                    singleLine = true
                )
            }
''')
    
    if skip:
        if line.strip() == "singleLine = true":
            # the next line is probably `)`, then we stop skipping
            pass
        elif line.strip() == ")" and lines[i-1].strip() == "singleLine = true":
            skip = False
        continue

    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/BillingScreen.kt', 'w') as f:
    f.writelines(new_lines)
