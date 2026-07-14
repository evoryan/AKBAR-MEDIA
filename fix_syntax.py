import re

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "r") as f:
    content = f.read()

# Fix newline
content = content.replace(
    'text = "Support By:\n" + com.example.ui.data.SettingsManager.supportByText,',
    'text = "Support By:\\n" + com.example.ui.data.SettingsManager.supportByText,'
)

# Add import if needed
if "import kotlinx.coroutines.launch" not in content:
    content = "import kotlinx.coroutines.launch\n" + content

with open("app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt", "w") as f:
    f.write(content)
