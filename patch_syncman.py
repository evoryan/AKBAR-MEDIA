import re

with open('app/src/main/java/com/example/ui/data/remote/SyncManager.kt', 'r') as f:
    content = f.read()

content = content.replace(
    "val client = OkHttpClient()",
    "val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor()).build()"
)

with open('app/src/main/java/com/example/ui/data/remote/SyncManager.kt', 'w') as f:
    f.write(content)
