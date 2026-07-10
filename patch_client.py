import re

with open('app/src/main/java/com/example/ui/data/remote/ApiClient.kt', 'r') as f:
    content = f.read()

content = content.replace(
    ".addInterceptor(OfflineInterceptor(database!!, context))",
    ".addInterceptor(AuthInterceptor())\n                .addInterceptor(OfflineInterceptor(database!!, context))"
)

with open('app/src/main/java/com/example/ui/data/remote/ApiClient.kt', 'w') as f:
    f.write(content)
