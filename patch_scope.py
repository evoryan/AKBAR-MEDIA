import re

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "r") as f:
    content = f.read()

target = """        } finally {
            isLoading = false
        }
val coroutineScope = rememberCoroutineScope()
    }"""

rep = """        } finally {
            isLoading = false
        }
    }
    
    val coroutineScope = rememberCoroutineScope()"""

content = content.replace(target, rep)

with open("app/src/main/java/com/example/ui/screens/SemuaPembukuanScreen.kt", "w") as f:
    f.write(content)
