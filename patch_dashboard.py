import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Make sure Toast is imported
if "import android.widget.Toast" not in content:
    content = content.replace("import androidx.compose.ui.unit.sp\n", "import androidx.compose.ui.unit.sp\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext\n")

old_header = """            Column {
                Text(com.example.ui.data.SettingsManager.companyName, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = textMain)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Halo, Admin", fontSize = 16.sp, color = textSecondary)
            }
            Box("""
new_header = """            Column {
                val context = LocalContext.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(com.example.ui.data.SettingsManager.companyName, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = textMain)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { 
                        viewModel.fetchData(context)
                        Toast.makeText(context, "Refreshing data...", Toast.LENGTH_SHORT).show()
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Halo, Admin", fontSize = 16.sp, color = textSecondary)
            }
            Box("""
content = content.replace(old_header, new_header)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
