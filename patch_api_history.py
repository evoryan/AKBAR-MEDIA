import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

models = """
data class PaymentHistory(
    val id: String,
    val type: String,
    val amount: String,
    val description: String,
    @com.squareup.moshi.Json(name = "created_at") val createdAt: String? = null
)
"""

if "PaymentHistory" not in content:
    content = content.replace("package com.example.ui.data.remote\n\n", "package com.example.ui.data.remote\n\n" + models)

endpoint = """
    @GET("api/customers/{id}/history")
    suspend fun getCustomerHistory(@Path("id") id: String): List<PaymentHistory>
"""

if "getCustomerHistory" not in content:
    content = content.replace('suspend fun addCustomer(@Body customer: com.example.ui.screens.Customer): ApiResponse', 'suspend fun addCustomer(@Body customer: com.example.ui.screens.Customer): ApiResponse\n' + endpoint)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
