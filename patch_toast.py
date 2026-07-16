import re

with open("app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt", "r") as f:
    content = f.read()

content = content.replace(
    """android.widget.Toast.makeText(context, "Gagal disable secret", android.widget.Toast.LENGTH_SHORT).show()""",
    """android.widget.Toast.makeText(context, "Gagal disable secret: ${e.message}", android.widget.Toast.LENGTH_LONG).show()"""
)

content = content.replace(
    """android.widget.Toast.makeText(context, "Gagal enable secret", android.widget.Toast.LENGTH_SHORT).show()""",
    """android.widget.Toast.makeText(context, "Gagal enable secret: ${e.message}", android.widget.Toast.LENGTH_LONG).show()"""
)

content = content.replace(
    """android.widget.Toast.makeText(context, "Gagal remove active", android.widget.Toast.LENGTH_SHORT).show()""",
    """android.widget.Toast.makeText(context, "Gagal remove active: ${e.message}", android.widget.Toast.LENGTH_LONG).show()"""
)

with open("app/src/main/java/com/example/ui/screens/ManageSecretsScreen.kt", "w") as f:
    f.write(content)
