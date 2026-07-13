with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

payment_history = """data class PaymentHistory(
    val id: String,
    val type: String,
    val amount: String,
    val description: String,
    @com.squareup.moshi.Json(name = "created_at") val createdAt: String? = null
)
"""

content = content.replace(payment_history, "")
content = content + "\n" + payment_history

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
