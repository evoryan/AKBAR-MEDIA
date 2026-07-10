import re

with open('app/src/main/java/com/example/ui/data/remote/SyncManager.kt', 'r') as f:
    content = f.read()

content = content.replace(
    ".addInterceptor(AuthInterceptor())",
    ".addInterceptor(AuthInterceptor(context))"
)

with open('app/src/main/java/com/example/ui/data/remote/SyncManager.kt', 'w') as f:
    f.write(content)
