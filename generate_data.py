filepath = 'app/src/main/java/com/example/ui/data/MockOdcOdpData.kt'
content = """package com.example.ui.data

import kotlinx.coroutines.flow.MutableStateFlow

data class OdcItem(val id: String, var name: String, var location: String)
data class OdpItem(val id: String, var name: String, var odcId: String, var portCount: Int)

object MockOdcOdpData {
    val odcList = MutableStateFlow(listOf(
        OdcItem("1", "ODC-01", "Pusat"),
        OdcItem("2", "ODC-02", "Cabang Utara")
    ))
    
    val odpList = MutableStateFlow(listOf(
        OdpItem("1", "ODP-01", "1", 8),
        OdpItem("2", "ODP-02", "1", 16),
        OdpItem("3", "ODP-03", "2", 8)
    ))
}
"""
with open(filepath, 'w') as f:
    f.write(content)
