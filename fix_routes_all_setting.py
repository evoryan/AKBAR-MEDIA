filepath = 'app/src/main/java/com/example/ui/navigation/Routes.kt'
with open(filepath, 'r') as f:
    content = f.read()

new_routes = """
@Serializable
object UpdateEmailRoute
@Serializable
object UpdateProfilRoute
@Serializable
object GantiPasswordRoute
@Serializable
object GantiPinRoute
@Serializable
object DaftarAdminRoute
@Serializable
object OdcRoute
@Serializable
object OdpRoute
@Serializable
object GatewayPaymentRoute
"""

if "UpdateEmailRoute" not in content:
    content = content + new_routes
    with open(filepath, 'w') as f:
        f.write(content)
