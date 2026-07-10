with open('app/src/main/java/com/example/ui/navigation/Routes.kt', 'r') as f:
    content = f.read()

content = content.replace("object BillingRoute", "data class BillingRoute(val initialTab: Int = 0)")
content = content.replace("object SemuaPembukuanRoute", "data class SemuaPembukuanRoute(val initialType: String = \"\")")

with open('app/src/main/java/com/example/ui/navigation/Routes.kt', 'w') as f:
    f.write(content)
