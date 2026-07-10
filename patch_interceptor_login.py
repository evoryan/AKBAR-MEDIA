import re

with open('app/src/main/java/com/example/ui/data/remote/OfflineInterceptor.kt', 'r') as f:
    content = f.read()

# For GET
get_logic = """if (request.method == "GET") {"""
new_get_logic = """if (request.method == "GET") {
            if (urlPath.contains("/api/login")) return chain.proceed(request)"""
content = content.replace(get_logic, new_get_logic)

# For POST
post_logic = """} else {
            // POST, PUT, DELETE"""
new_post_logic = """} else {
            // POST, PUT, DELETE
            if (urlPath.contains("/api/login")) return chain.proceed(request)"""
content = content.replace(post_logic, new_post_logic)

with open('app/src/main/java/com/example/ui/data/remote/OfflineInterceptor.kt', 'w') as f:
    f.write(content)
