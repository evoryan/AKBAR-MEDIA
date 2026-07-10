import re

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'r') as f:
    content = f.read()

# Fix the broken delete paths
content = re.sub(r'(@DELETE\("api/[\w]+/)(\{id\s+@GET\("api/acs/devices"\)\s+suspend fun getAcsDevices\(\): List<AcsDevice>\s+@POST\("api/acs/devices/\{id\}/action"\)\s+suspend fun acsAction\(@Path\("id"\) id: String, @Body request: Map<String, String>\): ApiResponse\}\)"\)', r'\1{id}")', content)

# Remove the broken repeated ACS methods
content = re.sub(r'\{id\s+@GET.*?ApiResponse\}\)"\)', '{id}")', content, flags=re.DOTALL)
content = re.sub(r'\{id\s+@GET.*?ApiResponse', '{id}")', content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/data/remote/ApiService.kt', 'w') as f:
    f.write(content)

