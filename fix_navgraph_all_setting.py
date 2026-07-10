filepath = 'app/src/main/java/com/example/ui/navigation/NavGraph.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Replace SettingScreen call
old_setting = """            composable<SettingRoute> {
                SettingScreen(onBack = { navController.popBackStack() })
            }"""

new_setting = """            composable<SettingRoute> {
                SettingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToUpdateEmail = { navController.navigate(UpdateEmailRoute) },
                    onNavigateToUpdateProfil = { navController.navigate(UpdateProfilRoute) },
                    onNavigateToGantiPassword = { navController.navigate(GantiPasswordRoute) },
                    onNavigateToGantiPin = { navController.navigate(GantiPinRoute) },
                    onNavigateToDaftarAdmin = { navController.navigate(DaftarAdminRoute) },
                    onNavigateToOdc = { navController.navigate(OdcRoute) },
                    onNavigateToOdp = { navController.navigate(OdpRoute) },
                    onNavigateToGatewayPayment = { navController.navigate(GatewayPaymentRoute) },
                    onLogout = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            
            composable<UpdateEmailRoute> { UpdateEmailScreen(onBack = { navController.popBackStack() }) }
            composable<UpdateProfilRoute> { UpdateProfilScreen(onBack = { navController.popBackStack() }) }
            composable<GantiPasswordRoute> { GantiPasswordScreen(onBack = { navController.popBackStack() }) }
            composable<GantiPinRoute> { GantiPinScreen(onBack = { navController.popBackStack() }) }
            composable<DaftarAdminRoute> { DaftarAdminScreen(onBack = { navController.popBackStack() }) }
            composable<OdcRoute> { OdcScreen(onBack = { navController.popBackStack() }) }
            composable<OdpRoute> { OdpScreen(onBack = { navController.popBackStack() }) }
            composable<GatewayPaymentRoute> { GatewayPaymentScreen(onBack = { navController.popBackStack() }) }"""

if old_setting in content:
    content = content.replace(old_setting, new_setting)

with open(filepath, 'w') as f:
    f.write(content)
