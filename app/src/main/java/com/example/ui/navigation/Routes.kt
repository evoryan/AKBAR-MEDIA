package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

@Serializable
object LoginRoute

@Serializable
object DashboardRoute

@Serializable
object CustomersRoute

@Serializable
data class BillingRoute(val initialTab: Int = 0)

@Serializable
object MikrotikRoute

@Serializable
data class AcsRoute(val searchQuery: String = "")

@Serializable
data class ManageSecretsRoute(val areaId: String)

@Serializable
object AddCustomerRoute

@Serializable
data class CustomerDetailRoute(val customerId: String)

@Serializable
data class PaymentRoute(val customerId: String)

@Serializable
data class PaymentSuccessRoute(val customerId: String, val totalAmount: String, val months: String)

@Serializable
object PackagesRoute

@Serializable
object AreaRoute

@Serializable
object BotWaRoute

@Serializable
object PembukuanRoute

@Serializable
object UangDiAdminRoute

@Serializable
object PembayaranByAdminRoute

@Serializable
data class SemuaPembukuanRoute(val initialType: String = "")

@Serializable
object RangkumanRoute

@Serializable
object StockBarangRoute

@Serializable
object InventoryRoute

@Serializable
object KategoriRoute

@Serializable
object HistoryStockRoute

@Serializable
object SettingRoute

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

@Serializable
object CompanySettingsRoute

@Serializable
object BackupRestoreRoute
