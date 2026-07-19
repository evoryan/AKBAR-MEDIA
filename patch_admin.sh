#!/bin/bash
sed -i '244a \
                    Box {\
                        OutlinedButton(\
                            onClick = { areaExpanded = true },\
                            modifier = Modifier.fillMaxWidth(),\
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),\
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)\
                        ) {\
                            Text(if (selectedArea == "semua") "Semua Area" else areas.find { it.id == selectedArea }?.nama ?: selectedArea)\
                        }\
                        DropdownMenu(\
                            expanded = areaExpanded,\
                            onDismissRequest = { areaExpanded = false },\
                            modifier = Modifier.background(cardBg).border(1.dp, cardBorder)\
                        ) {\
                            DropdownMenuItem(\
                                text = { Text("Semua Area", color = textMain) },\
                                onClick = {\
                                    selectedArea = "semua"\
                                    areaExpanded = false\
                                }\
                            )\
                            areas.forEach { area ->\
                                DropdownMenuItem(\
                                    text = { Text(area.nama, color = textMain) },\
                                    onClick = {\
                                        selectedArea = area.id\
                                        areaExpanded = false\
                                    }\
                                )\
                            }\
                        }\
                    }\
' app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt
