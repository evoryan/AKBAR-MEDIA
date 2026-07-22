package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.delay
import com.example.ui.data.remote.ApiClient
import com.example.ui.components.FloatingNavBar
import com.example.ui.screens.*

@Composable
fun AkbarMediaNavGraph() {
    val navController = rememberNavController()
    var isServerOnline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while(true) {
            try {
                ApiClient.apiService.ping()
                isServerOnline = true
            } catch(e: Exception) {
                isServerOnline = false
            }
            delay(5000)
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    val hideBottomNavRoutes = listOf(
        "com.example.ui.navigation.SplashRoute",
        "com.example.ui.navigation.LoginRoute"
    )

    val showBottomNav = currentDestination != null && !hideBottomNavRoutes.any { currentDestination.contains(it) }

    Box(modifier = Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background)) {
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            modifier = Modifier.fillMaxSize().padding(bottom = if (showBottomNav) 104.dp else 0.dp),
            enterTransition = { fadeIn(animationSpec = tween(100)) },
            exitTransition = { fadeOut(animationSpec = tween(100)) },
            popEnterTransition = { fadeIn(animationSpec = tween(100)) },
            popExitTransition = { fadeOut(animationSpec = tween(100)) }
        ) {
            composable<NocDashboardRoute> {
                NocDashboardScreen(
                    onNavigateToAcs = { navController.navigate(AcsRoute("")) },
                    onNavigateToMikrotik = { navController.navigate(MikrotikRoute) },
                    onNavigateToJaringan = { navController.navigate(JaringanRoute) },
                    onNavigateToPelanggan = { navController.navigate(CustomersRoute) },
                    onNavigateToPembukuan = { navController.navigate(SemuaPembukuanRoute()) },
                    onNavigateToSetting = { navController.navigate(SettingRoute) },
                    onNavigateToDashboard = { navController.navigate(DashboardRoute) },
                    onNavigateToManageSecrets = { areaId, filter ->
                        navController.navigate(ManageSecretsRoute(areaId = areaId, initialFilter = filter))
                    },
                    onNavigateToStockBarang = { navController.navigate(StockBarangRoute) }
                )
            }

            composable<SplashRoute> {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(LoginRoute) {
                            popUpTo(SplashRoute) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        val currentUser = com.example.ui.data.UserSession.currentUser.value
                        if (currentUser?.role == com.example.ui.data.UserRole.TEKNISI) {
                            navController.navigate(NocDashboardRoute) {
                                popUpTo(SplashRoute) { inclusive = true }
                            }
                        } else {
                            navController.navigate(DashboardRoute) {
                                popUpTo(SplashRoute) { inclusive = true }
                            }
                        }
                    }
                )
            }
            
            composable<LoginRoute> {
                LoginScreen(
                    onLoginSuccess = {
                        val currentUser = com.example.ui.data.UserSession.currentUser.value
                        if (currentUser?.role == com.example.ui.data.UserRole.TEKNISI) {
                            navController.navigate(NocDashboardRoute) {
                                popUpTo(LoginRoute) { inclusive = true }
                            }
                        } else {
                            navController.navigate(DashboardRoute) {
                                popUpTo(LoginRoute) { inclusive = true }
                            }
                        }
                    }
                )
            }
            
            composable<DashboardRoute> {
                DashboardScreen(
                    onNavigateToCustomers = { navController.navigate(CustomersRoute) },
                    onNavigateToBilling = { navController.navigate(BillingRoute()) },
                    onNavigateToMikrotik = { navController.navigate(MikrotikRoute) },
                    onNavigateToPackages = { navController.navigate(PackagesRoute) },
                    onNavigateToArea = { navController.navigate(AreaRoute) },
                    onNavigateToAcs = { navController.navigate(AcsRoute()) },
                    onNavigateToBotWa = { navController.navigate(BotWaRoute) },
                    onNavigateToPembukuan = { navController.navigate(PembukuanRoute) },
                    onNavigateToStockBarang = { navController.navigate(StockBarangRoute) },
                    onNavigateToSetting = { navController.navigate(SettingRoute) },
                    onNavigateToJaringan = { navController.navigate(JaringanRoute) },
                    onNavigateToProfile = { navController.navigate(ProfileRoute) }
                )
            }

            
            composable<JaringanRoute> {
                com.example.ui.screens.JaringanScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<CustomersRoute> {
                CustomersScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToCustomerDetail = { customerId ->
                        navController.navigate(CustomerDetailRoute(customerId))
                    },
                    onNavigateToAddCustomer = { navController.navigate(AddCustomerRoute) },
                    onNavigateToEditCustomer = { customerId -> 
                        navController.navigate(EditCustomerRoute(customerId))
                    }
                )
            }

            composable<AddCustomerRoute> {
                AddCustomerScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<EditCustomerRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<EditCustomerRoute>()
                com.example.ui.screens.EditCustomerScreen(
                    customerId = route.customerId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable<CustomerDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<CustomerDetailRoute>()
                CustomerDetailScreen(
                    customerId = route.customerId,
                    onBack = { navController.popBackStack() },
                    onNavigateToPayment = { id -> navController.navigate(PaymentRoute(id)) },
                    onNavigateToAcs = { query -> navController.navigate(AcsRoute(query)) }
                )
            }

            composable<BillingRoute> {
                val args = it.toRoute<BillingRoute>()
                BillingScreen(
                    initialTab = args.initialTab,
                    onBack = { navController.popBackStack() },
                    onNavigateToPayment = { customerId ->
                        navController.navigate(PaymentRoute(customerId))
                    },
                    onNavigateToSuccess = { customerId, totalAmount, months ->
                        navController.navigate(PaymentSuccessRoute(customerId, totalAmount, months))
                    }
                )
            }

            composable<PaymentRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<PaymentRoute>()
                PaymentScreen(
                    customerId = route.customerId,
                    onBack = { navController.popBackStack() },
                    onNavigateToDetail = { navController.navigate(CustomerDetailRoute(route.customerId)) },
                    onNavigateToSuccess = { customerId, totalAmount, months -> 
                        navController.navigate(PaymentSuccessRoute(customerId, totalAmount, months)) {
                            popUpTo(PaymentRoute(customerId)) { inclusive = true }
                        }
                    }
                )
            }

            composable<PaymentSuccessRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<PaymentSuccessRoute>()
                PaymentSuccessScreen(
                    customerId = route.customerId,
                    totalAmount = route.totalAmount,
                    months = route.months,
                    onFinish = { 
                        navController.navigate(BillingRoute()) {
                            popUpTo(BillingRoute::class) { inclusive = true }
                        }
                    }
                )
            }

            composable<PackagesRoute> {
                PackagesScreen(onBack = { navController.popBackStack() })
            }

            composable<AreaRoute> {
                AreaScreen(onBack = { navController.popBackStack() })
            }
            
            composable<MikrotikRoute> {
                MikrotikScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToManageSecrets = { areaId -> navController.navigate(ManageSecretsRoute(areaId)) }
                )
            }


            composable<AcsRoute> { backStackEntry ->
                val query = backStackEntry.toRoute<AcsRoute>().searchQuery
                AcsScreen(onBack = { navController.popBackStack() }, initialSearchQuery = query)
            }
            composable<BotWaRoute> {
                BotWaScreen(onBack = { navController.popBackStack() })
            }
            composable<PembukuanRoute> {
                PembukuanScreen(
                    onNavigateToBilling = { tab -> navController.navigate(BillingRoute(tab)) },
                    onBack = { navController.popBackStack() },
                    onNavigateToUangDiAdmin = { navController.navigate(UangDiAdminRoute) },
                    onNavigateToPembayaranByAdmin = { navController.navigate(PembayaranByAdminRoute) },
                    onNavigateToSemuaPembukuan = { type -> navController.navigate(SemuaPembukuanRoute(type)) },
                    onNavigateToRangkuman = { navController.navigate(RangkumanRoute) }
                )
            }
            composable<UangDiAdminRoute> {
                UangDiAdminScreen(onBack = { navController.popBackStack() })
            }
            composable<PembayaranByAdminRoute> {
                PembayaranByAdminScreen(onBack = { navController.popBackStack() })
            }
            composable<SemuaPembukuanRoute> {
                val args = it.toRoute<SemuaPembukuanRoute>()
                SemuaPembukuanScreen(initialType = args.initialType, onBack = { navController.popBackStack() })
            }
            composable<RangkumanRoute> {
                RangkumanScreen(onBack = { navController.popBackStack() })
            }

            
            composable<StockBarangRoute> {
                StockBarangScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToInventory = { navController.navigate(InventoryRoute) },
                    onNavigateToKategori = { navController.navigate(KategoriRoute) },
                    onNavigateToHistory = { navController.navigate(HistoryStockRoute) }
                )
            }
            
            composable<InventoryRoute> {
                InventoryScreen(onBack = { navController.popBackStack() })
            }
            
            composable<KategoriRoute> {
                KategoriScreen(onBack = { navController.popBackStack() })
            }
            
            composable<HistoryStockRoute> {
                HistoryStockScreen(onBack = { navController.popBackStack() })
            }
            
            composable<SettingRoute> {
                SettingScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToUpdateEmail = { navController.navigate(UpdateEmailRoute) },
                    onNavigateToUpdateProfil = { navController.navigate(UpdateProfilRoute) },
                    onNavigateToGantiPassword = { navController.navigate(GantiPasswordRoute) },
                    onNavigateToGantiPin = { navController.navigate(GantiPinRoute) },
                    onNavigateToDaftarAdmin = { navController.navigate(DaftarAdminRoute) },
                    onNavigateToOdc = { navController.navigate(OdcRoute) },
                    onNavigateToOdp = { navController.navigate(OdpRoute) },
                    onNavigateToRasio = { navController.navigate(RasioRoute) },
                    onNavigateToGatewayPayment = { navController.navigate(GatewayPaymentRoute) },
                    onNavigateToCompanySettings = { navController.navigate(CompanySettingsRoute) },
                    onNavigateToBackupRestore = { navController.navigate(BackupRestoreRoute) },
                    onNavigateToInvoiceSettings = { navController.navigate(InvoiceSettingsRoute) },
                                        onLogout = {
                        val context = navController.context
                        com.example.ui.data.UserSession.clearSession(context)
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            
            composable<UpdateEmailRoute> { UpdateEmailScreen(onBack = { navController.popBackStack() }) }
            composable<UpdateProfilRoute> { UpdateProfilScreen(onBack = { navController.popBackStack() }) }
            composable<GantiPasswordRoute> { 
                GantiPasswordScreen(
                    onBack = { navController.popBackStack() },
                    onPasswordChanged = {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) 
            }
            composable<GantiPinRoute> { GantiPinScreen(onBack = { navController.popBackStack() }) }
            composable<DaftarAdminRoute> { DaftarAdminScreen(onBack = { navController.popBackStack() }) }
            composable<OdcRoute> { OdcScreen(onBack = { navController.popBackStack() }) }
            
            composable<RasioRoute> {
                com.example.ui.screens.RasioScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<OdpRoute> { OdpScreen(onBack = { navController.popBackStack() }) }
            composable<GatewayPaymentRoute> { GatewayPaymentScreen(onBack = { navController.popBackStack() }) }
            composable<CompanySettingsRoute> { com.example.ui.screens.CompanySettingsScreen(onBack = { navController.popBackStack() }) }
            composable<BackupRestoreRoute> { com.example.ui.screens.BackupRestoreScreen(onBack = { navController.popBackStack() }) }
            composable<InvoiceSettingsRoute> { com.example.ui.screens.InvoiceSettingsScreen(onBack = { navController.popBackStack() }) }
            composable<ProfileRoute> {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToEditProfile = { navController.navigate(UpdateProfilRoute) },
                    onLogout = {
                        val context = navController.context
                        com.example.ui.data.UserSession.clearSession(context)
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable<ManageSecretsRoute> {
                val route = it.toRoute<ManageSecretsRoute>()
                ManageSecretsScreen(
                    areaId = route.areaId,
                    initialFilter = route.initialFilter,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        
        if (showBottomNav) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingNavBar(
                    currentRoute = currentDestination,
                    onNavigateToDashboard = { 
                        val currentUser = com.example.ui.data.UserSession.currentUser.value
                        if (currentUser?.role == com.example.ui.data.UserRole.TEKNISI) {
                            navController.navigate(NocDashboardRoute) {
                                popUpTo(NocDashboardRoute) { inclusive = true }
                            }
                        } else {
                            navController.navigate(DashboardRoute) {
                                popUpTo(DashboardRoute) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToCustomers = { navController.navigate(CustomersRoute) },
                    onNavigateToBilling = { navController.navigate(BillingRoute()) },
                    onNavigateToSettings = { navController.navigate(SettingRoute) }
                )
            }
        }
        
        // Online/Offline Indicator (Visible on all screens)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .statusBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isServerOnline) Color.Green else Color.Red)
            )
        }
    }
}
