import re

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("                                    portCount = port,\n                                    portInput = portInput\n                                )", "                                    portCount = port,\n                                    portInput = portInput,\n                                    redamanIn = redamanIn,\n                                    redamanOut = redamanOut\n                                )")

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(content)
