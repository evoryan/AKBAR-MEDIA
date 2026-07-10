import re

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'r') as f:
    content = f.read()

# Fix the duplicate block
# Remove this block entirely:
block_to_remove = """        try {
            val res = ApiClient.apiService.getPembukuan()
            pemasukan = res.pemasukan
            pengeluaran = res.pengeluaran
        } catch (e: Exception) {
            // Error handling ignored for now
        }
    }"""
content = content.replace(block_to_remove, "")

with open('app/src/main/java/com/example/ui/screens/PembukuanScreen.kt', 'w') as f:
    f.write(content)
