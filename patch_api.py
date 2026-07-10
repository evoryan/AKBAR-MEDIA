with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

endpoints = """
    @GET("api/pengeluaran")
    suspend fun getPengeluaranDetail(): List<PengeluaranItem>

    @POST("api/pengeluaran")
    suspend fun updatePengeluaranDetail(@Body request: PengeluaranRequest): ApiResponse
"""
content = content.replace("suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse", "suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse\n" + endpoints)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
