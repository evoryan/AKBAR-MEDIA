#!/bin/bash
awk '
/Box \{/ {
    box_count++
}
box_count == 3 && /}/ {
    print $0
    print "                    Box {"
    print "                        OutlinedButton("
    print "                            onClick = { areaExpanded = true },"
    print "                            modifier = Modifier.fillMaxWidth(),"
    print "                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),"
    print "                            border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)"
    print "                        ) {"
    print "                            Text(if (selectedArea == \"semua\") \"Semua Area\" else areas.find { it.id == selectedArea }?.nama ?: selectedArea)"
    print "                        }"
    print "                        DropdownMenu("
    print "                            expanded = areaExpanded,"
    print "                            onDismissRequest = { areaExpanded = false },"
    print "                            modifier = Modifier.background(cardBg).border(1.dp, cardBorder)"
    print "                        ) {"
    print "                            DropdownMenuItem("
    print "                                text = { Text(\"Semua Area\", color = textMain) },"
    print "                                onClick = {"
    print "                                    selectedArea = \"semua\""
    print "                                    areaExpanded = false"
    print "                                }"
    print "                            )"
    print "                            areas.forEach { area ->"
    print "                                DropdownMenuItem("
    print "                                    text = { Text(area.nama, color = textMain) },"
    print "                                    onClick = {"
    print "                                        selectedArea = area.id"
    print "                                        areaExpanded = false"
    print "                                    }"
    print "                                )"
    print "                            }"
    print "                        }"
    print "                    }"
    box_count = 0
    next
}
{ print }
' app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt > tmp_admin.kt
mv tmp_admin.kt app/src/main/java/com/example/ui/screens/DaftarAdminScreen.kt
