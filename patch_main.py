import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

target_import = "import com.example.ui.theme.AppTheme"
rep_import = """import com.example.ui.theme.AppTheme
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts"""

content = content.replace(target_import, rep_import)

target_create = """  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)"""
rep_create = """  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) {}

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
"""
content = content.replace(target_create, rep_create)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
