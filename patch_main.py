with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

if "ApiClient.init" not in content:
    content = content.replace(
        "super.onCreate(savedInstanceState)", 
        "super.onCreate(savedInstanceState)\n    com.example.ui.data.remote.ApiClient.init(applicationContext)"
    )

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
