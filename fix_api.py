with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

content = content.replace("val created_at: String?,\n    val admin_name: String? = null", "val created_at: String?")

# Now carefully add it just to PembukuanItem
old_pembukuan = """data class PembukuanItem(
    val id: String,
    val type: String,
    val category: String?,
    val amount: Double,
    val description: String?,
    val created_at: String?
)"""
new_pembukuan = """data class PembukuanItem(
    val id: String,
    val type: String,
    val category: String?,
    val amount: Double,
    val description: String?,
    val created_at: String?,
    val admin_name: String? = null
)"""

content = content.replace(old_pembukuan, new_pembukuan)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
