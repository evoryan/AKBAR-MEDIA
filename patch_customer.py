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
    val additionalCost2: String? = null
)"""

content = re.sub(
    r"data class Customer\(\s*val id: String,\s*val name: String,\s*val phone: String,\s*val area: String,\s*val username: String,\s*val billingDate: String,\s*val status: String,\s*val price: String,\s*val discount: String\s*\)",
    new_customer_class,
    content
)

with open('app/src/main/java/com/example/ui/screens/CustomersScreen.kt', 'w') as f:
    f.write(content)
