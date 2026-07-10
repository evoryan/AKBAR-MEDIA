import re

filepath = 'app/src/main/java/com/example/ui/screens/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Variables to add
variables = """
    var selectedMonth by remember { mutableStateOf("Agustus") }
    var selectedYear by remember { mutableStateOf("2026") }
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }
    
    val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val years = listOf("2023", "2024", "2025", "2026", "2027")

    val bgMain = Color(0xFF0A0A0A)"""

content = content.replace('val bgMain = Color(0xFF0A0A0A)', variables)

# UI to add after Stack Informasi
ui_to_add = """
        // Filter Bulan & Tahun
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dropdown Bulan
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandedMonth = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                    border = border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedMonth, color = textMain, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMain)
                    }
                }
                DropdownMenu(
                    expanded = expandedMonth,
                    onDismissRequest = { expandedMonth = false },
                    modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                ) {
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month, color = textMain) },
                            onClick = {
                                selectedMonth = month
                                expandedMonth = false
                            }
                        )
                    }
                }
            }
            
            // Dropdown Tahun
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { expandedYear = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                    border = border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(selectedYear, color = textMain, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMain)
                    }
                }
                DropdownMenu(
                    expanded = expandedYear,
                    onDismissRequest = { expandedYear = false },
                    modifier = Modifier.background(cardBg).border(1.dp, cardBorder)
                ) {
                    years.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year, color = textMain) },
                            onClick = {
                                selectedYear = year
                                expandedYear = false
                            }
                        )
                    }
                }
            }
        }
        
        // Total Pendapatan Bulan Terkait
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryBg.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = primaryBg, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Total Pendapatan ($selectedMonth $selectedYear)", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textSecondary)
                }
                
                when (val state = uiState) {
                    is DashboardState.Loading -> Text("Rp ...", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = textMain)
                    is DashboardState.Success -> {
                        val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID"))
                        format.maximumFractionDigits = 0
                        Text(format.format(state.data.monthlyRevenue), fontWeight = FontWeight.Bold, fontSize = 32.sp, color = primaryBg)
                    }
                    is DashboardState.Error -> Text("-", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = textMain)
                }
            }
        }

        // Summary Grid"""

content = content.replace('// Summary Grid', ui_to_add)

# Make border function available
content = content.replace("import androidx.compose.foundation.border", "import androidx.compose.foundation.border\nimport androidx.compose.foundation.BorderStroke as border")


with open(filepath, 'w') as f:
    f.write(content)
