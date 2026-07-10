import re

filepath = 'app/src/main/java/com/example/ui/screens/LoginScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

if "import com.example.ui.data.MockAdminData" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport com.example.ui.data.MockAdminData\nimport com.example.ui.data.UserSession\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext")

content = content.replace("fun LoginScreen(onLoginSuccess: () -> Unit) {", "fun LoginScreen(onLoginSuccess: () -> Unit) {\n    val context = LocalContext.current")

login_action = """onClick = {
                    val user = MockAdminData.adminList.value.find { it.username == username && password.isNotEmpty() }
                    if (user != null) {
                        UserSession.currentUser.value = user
                        onLoginSuccess()
                    } else {
                        Toast.makeText(context, "Username atau password salah", Toast.LENGTH_SHORT).show()
                    }
                }"""

content = re.sub(r"onClick = onLoginSuccess,", login_action + ",", content)

with open(filepath, 'w') as f:
    f.write(content)
