with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

bad_str = """package com.example.ui.data.remote

data class PaymentHistory(
    val id: String,
    val type: String,
    val amount: String,
    val description: String,
    @com.squareup.moshi.Json(name = "created_at") val createdAt: String? = null
)

import com.example.ui.data.AdminUser"""

good_str = """package com.example.ui.data.remote

import com.example.ui.data.AdminUser

data class PaymentHistory(
    val id: String,
    val type: String,
    val amount: String,
    val description: String,
    @com.squareup.moshi.Json(name = "created_at") val createdAt: String? = null
)"""

content = content.replace(bad_str, good_str)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
