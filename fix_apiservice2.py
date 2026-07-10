import re
filepath = 'app/src/main/java/com/example/ui/data/remote/ApiService.kt'
with open(filepath, 'r') as f:
    content = f.read()

endpoints = """
    @DELETE("api/customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: String)

    @POST("api/customers")
    suspend fun addCustomer(@Body customer: com.example.ui.screens.Customer): com.example.ui.screens.Customer
"""

content = content.rstrip()
if content.endswith('}'):
    content = content[:-1] + endpoints + "\n}"

with open(filepath, 'w') as f:
    f.write(content)
