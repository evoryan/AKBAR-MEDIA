import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

content = content.replace("import retrofit2.http.GET\nimport retrofit2.http.POST\nimport retrofit2.http.PUT\nimport retrofit2.http.DELETE\nimport retrofit2.http.Path\nimport retrofit2.http.Body", "import retrofit2.http.*")

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)
