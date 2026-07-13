with open('app/src/main/java/com/example/ui/navigation/Routes.kt', 'r') as f:
    content = f.read()

content = content.replace("object GatewayPaymentRoute", "object GatewayPaymentRoute\n\n@kotlinx.serialization.Serializable\nobject CompanySettingsRoute")

with open('app/src/main/java/com/example/ui/navigation/Routes.kt', 'w') as f:
    f.write(content)
