1. **Setting Menu**: NavGraph `onNavigateToSettings` now routes to `SettingRoute`.
2. **Packages Menu**: Saved via VPS backend (`addPackage`, `deletePackage`) using Coroutines. Removed PPPoE profile code in PackagesScreen.kt and Item layout.
3. **Billing Unpaid List**: Fetches real customers data from API, deletes "Belum Bayar" using API request `deleteBilling` returning real changes, clicking Trash deletes the record.
4. **Dashboard PPPoE Offline**: VPS `/api/dashboard/pppoe-offline` queries RouterOS for Secrets that are not in PPP/Active list. Dashboard gets the offline list via API and shows them.
5. **Payment Notifications**: API endpoint `payBilling` adds entry to `pembukuan` and `notifications` ("Pembayaran <pelanggan> di terima oleh <admin>") as requested.
