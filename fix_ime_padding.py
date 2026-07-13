import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("Column(modifier = Modifier.fillMaxSize().padding(innerPadding).background(bgDark))", 
                          "Column(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding().background(bgDark))")

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(content)
