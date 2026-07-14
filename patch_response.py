with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "r") as f:
    content = f.read()

target = """data class PembukuanResponse(
    val pemasukan: Long,
    val pengeluaran: Long,
    val categories: Map<String, Long> = emptyMap()
)"""

replacement = """data class PembukuanResponse(
    val pemasukan: Double,
    val pengeluaran: Double,
    val categories: Map<String, Double> = emptyMap()
)"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/data/remote/ApiService.kt", "w") as f:
        f.write(content)
    print("Replaced PembukuanResponse successfully")
else:
    print("Target PembukuanResponse not found")
