with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

new_models = """
data class PppoeOfflineResponse(val offlinePPPoE: Int)
data class PaymentRequest(val customerId: String, val adminName: String, val totalAmount: Double)
data class DeleteBillingRequest(val customerId: String)
"""

if "PppoeOfflineResponse" not in content:
    content = content.replace("interface ApiService {", new_models + "\ninterface ApiService {")

new_methods = """
    @GET("api/dashboard/pppoe-offline")
    suspend fun getPppoeOffline(): PppoeOfflineResponse

    @POST("api/billing/pay")
    suspend fun payBilling(@Body request: PaymentRequest): ApiResponse

    @POST("api/billing/delete")
    suspend fun deleteBilling(@Body request: DeleteBillingRequest): ApiResponse
"""

if "getPppoeOffline" not in content:
    content = content.replace("interface ApiService {", "interface ApiService {" + new_methods)
    with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
        f.write(content)
