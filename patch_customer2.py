import re

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'r') as f:
    content = f.read()

# Replace Customer data class
new_customer_class = """data class Customer(
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

content = re.sub(
    r"data class Customer\([\s\S]*?val additionalCost2: String\? = null\n\)",
    new_customer_class,
    content
)

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'w') as f:
    f.write(content)
