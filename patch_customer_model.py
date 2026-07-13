import re

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'r') as f:
    content = f.read()

imports = "import com.squareup.moshi.Json\n"

if "import com.squareup.moshi.Json" not in content:
    content = content.replace("package com.example.ui.screens\n\n", "package com.example.ui.screens\n\n" + imports)

old_data_class = """data class Customer(
    val id: String = "",
    val name: String,
    val phone: String,
    val area: String,
    val username: String,
    val billingDate: String,
    val status: String,
    val price: String,
    val discount: String,
    val registerDate: String? = null,
    val isolateDate: String? = null,
    val packageName: String? = null,
    val additionalCost1: String? = null,
    val additionalCost2: String? = null,
    val pppoeSecret: String? = null,
    val odpId: String? = null,
    val odpPort: String? = null
)"""

new_data_class = """data class Customer(
    val id: String = "",
    val name: String,
    val phone: String,
    val area: String,
    val username: String,
    val billingDate: String,
    val status: String,
    val price: String,
    val discount: String,
    @Json(name = "register_date") val registerDate: String? = null,
    @Json(name = "isolate_date") val isolateDate: String? = null,
    @Json(name = "package_name") val packageName: String? = null,
    val additionalCost1: String? = null,
    val additionalCost2: String? = null,
    @Json(name = "pppoe_secret") val pppoeSecret: String? = null,
    @Json(name = "odp_id") val odpId: String? = null,
    @Json(name = "odp_port") val odpPort: String? = null
)"""

content = content.replace(old_data_class, new_data_class)

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'w') as f:
    f.write(content)
