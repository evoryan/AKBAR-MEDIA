with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('                    }\n                    Text("Simpan", color = primaryBg)\n                }\n            },', '                    }\n                }) {\n                    Text("Simpan", color = primaryBg)\n                }\n            },')

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    content2 = f.read()

content2 = content2.replace('                    }\n                    Text("Simpan", color = primaryBg)\n                }\n            },', '                    }\n                }) {\n                    Text("Simpan", color = primaryBg)\n                }\n            },')

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(content2)
