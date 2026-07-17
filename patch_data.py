import re

with open('app/src/main/java/com/example/ui/data/OdcOdpData.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'data class OdcItem(val id: String, var name: String, var location: String, var portCount: Int = 0, var portInput: String = "")',
    'data class OdcItem(val id: String, var name: String, var location: String, var portCount: Int = 0, var portInput: String = "", @com.squareup.moshi.Json(name="redaman_in") var redamanIn: String = "", @com.squareup.moshi.Json(name="redaman_out") var redamanOut: String = "")'
)

content = content.replace(
    'data class OdpItem(val id: String, var name: String, var odcId: String, var portCount: Int = 0, var portInput: String = "")',
    'data class OdpItem(val id: String, var name: String, var odcId: String, var portCount: Int = 0, var portInput: String = "", @com.squareup.moshi.Json(name="redaman_in") var redamanIn: String = "", @com.squareup.moshi.Json(name="redaman_out") var redamanOut: String = "")'
)

with open('app/src/main/java/com/example/ui/data/OdcOdpData.kt', 'w') as f:
    f.write(content)
