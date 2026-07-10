with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

old_pembukuan_res = """data class PembukuanResponse(
    val pemasukan: Long,
    val pengeluaran: Long
)"""

new_pembukuan_res = """data class PembukuanResponse(
    val pemasukan: Long,
    val pengeluaran: Long,
    val categories: Map<String, Long> = emptyMap()
)

data class PembukuanRequest(
    val type: String,
    val category: String,
    val amount: Double,
    val description: String
)"""

content = content.replace(old_pembukuan_res, new_pembukuan_res)

if "addPembukuan" not in content:
    content = content.replace("suspend fun getPembukuan(): PembukuanResponse", "suspend fun getPembukuan(): PembukuanResponse\n\n    @POST(\"api/pembukuan\")\n    suspend fun addPembukuan(@Body request: PembukuanRequest): ApiResponse")

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
