#!/bin/bash
sed -i 's/var selectedMonth by remember { mutableStateOf("Juli 2026") }/var selectedMonth by remember { mutableStateOf("Semua Waktu") }\n    var isLoading by remember { mutableStateOf(true) }\n    var pembukuanData by remember { mutableStateOf<com.example.ui.data.remote.PembukuanResponse?>(null) }\n    LaunchedEffect(Unit) {\n        try {\n            pembukuanData = com.example.ui.data.remote.ApiClient.apiService.getPembukuan()\n        } catch(e: Exception) {}\n        finally { isLoading = false }\n    }/g' app/src/main/java/com/example/ui/screens/RangkumanScreen.kt

sed -i 's/val months = listOf("Mei 2026", "Juni 2026", "Juli 2026", "Agustus 2026")/val months = listOf("Semua Waktu")/g' app/src/main/java/com/example/ui/screens/RangkumanScreen.kt

sed -i 's/Text("Rp. 12.100.000", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)/Text(if(isLoading) "..." else "Rp. ${String.format("%,d", (pembukuanData?.pemasukan?.toLong() ?: 0L)).replace(",", ".")}", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)/g' app/src/main/java/com/example/ui/screens/RangkumanScreen.kt

sed -i 's/Text("Rp. 0", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)/Text(if(isLoading) "..." else "Rp. ${String.format("%,d", (pembukuanData?.categories?.get("Lain-lain")?.toLong() ?: 0L)).replace(",", ".")}", color = textMain, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)/g' app/src/main/java/com/example/ui/screens/RangkumanScreen.kt

sed -i 's/Text("Rp. 0", color = neonRed, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)/Text(if(isLoading) "..." else "Rp. ${String.format("%,d", (pembukuanData?.pengeluaran?.toLong() ?: 0L)).replace(",", ".")}", color = neonRed, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)/g' app/src/main/java/com/example/ui/screens/RangkumanScreen.kt

sed -i 's/Text("Rp. 12.100.000", color = neonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)/Text(if(isLoading) "..." else "Rp. ${String.format("%,d", ((pembukuanData?.pemasukan ?: 0.0) - (pembukuanData?.pengeluaran ?: 0.0)).toLong()).replace(",", ".")}", color = neonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)/g' app/src/main/java/com/example/ui/screens/RangkumanScreen.kt

