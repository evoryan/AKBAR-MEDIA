import re

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "r") as f:
    content = f.read()

# Add HorizontalPager import if missing
if "androidx.compose.foundation.pager.HorizontalPager" not in content:
    content = content.replace(
        "import androidx.compose.foundation.layout.*",
        "import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.pager.HorizontalPager\nimport androidx.compose.foundation.pager.rememberPagerState"
    )

# Find the Total Pendapatan Box and replace it
pattern = r"// Total Pendapatan Bulan Terkait.*?// Summary Grid"

replacement = """// Total Pendapatan (Swipeable)
        val pagerState = rememberPagerState(pageCount = { 2 })
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(28.dp))
                .padding(20.dp)
        ) {
            Column {
                HorizontalPager(state = pagerState) { page ->
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
                            Text(
                                if (page == 0) "Total Pendapatan ($selectedMonth $selectedYear)" else "Total Pendapatan Global",
                                fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textSecondary
                            )
                        }
                        
                        when (val state = uiState) {
                            is DashboardState.Loading -> Text("Rp ...", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = textMain)
                            is DashboardState.Success -> {
                                val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("id-ID"))
                                format.maximumFractionDigits = 0
                                val amount = if (page == 0) state.data.monthlyRevenue else state.data.totalGlobalRevenue
                                Text(format.format(amount), fontWeight = FontWeight.Bold, fontSize = 32.sp, color = primaryBg)
                            }
                            is DashboardState.Error -> Text("-", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = textMain)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(2) { iteration ->
                        val color = if (pagerState.currentPage == iteration) primaryBg else cardBorder
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(6.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Grid"""

content = re.sub(pattern, replacement, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/DashboardScreen.kt", "w") as f:
    f.write(content)

