with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "r") as f:
    content = f.read()

# Fix emptyList()
content = content.replace(
    "catch (e: Exception) { emptyList() }",
    "catch (e: Exception) { emptyList<com.example.ui.data.remote.UangAdminResponse>() }"
)

with open("app/src/main/java/com/example/ui/screens/UangDiAdminScreen.kt", "w") as f:
    f.write(content)
print("Fixed emptyList cast")
