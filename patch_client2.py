import re

with open('app/src/main/java/com/example/ui/data/remote/ApiClient.kt', 'r') as f:
    content = f.read()

content = content.replace(
    ".addInterceptor(AuthInterceptor())",
    ".addInterceptor(AuthInterceptor(context))"
)

with open('app/src/main/java/com/example/ui/data/remote/ApiClient.kt', 'w') as f:
    f.write(content)
