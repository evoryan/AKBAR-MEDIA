# Panduan Firebase Cloud Messaging (FCM) Realtime - Terisolasi Per Database / Tenant

Fitur Push Notification terisolasi ketat antar tenant/database telah diimplementasikan di backend dan aplikasi Android.
Setiap kali ada pembayaran masuk, notifikasi dikirim secara real-time HANYA ke admin/perangkat yang terhubung ke database tenant bersangkutan.

## Cara Kerja Isolasi Per Database
1. **Topik Spesifik Database**:
   Setiap tenant memiliki database masing-masing (misalnya `akbar_media_demo`, `tenant_cikarang`, dll).
   Notifikasi dikirim ke topik: `tenant_<sanitized_database_name>`.
2. **Tidak Ada Broadcast Global**:
   Notifikasi tidak dikirim ke topik global atau superadmin lintas database, sehingga data transaksi antar database tidak akan bocor atau tercampur.
3. **Validasi Sisi Klien (Android)**:
   Aplikasi Android memverifikasi bahwa `db_name` pada pesan FCM sesuai dengan database sesi user yang sedang aktif. Jika tidak cocok atau user sudah logout, notifikasi langsung dibuang dan topik lama di-unsubscribe secara otomatis.

## Persyaratan Server (VPS)
Untuk mengaktifkan pengiriman FCM dari server:
1. Unduh `serviceAccountKey.json` dari Firebase Console (**Project Settings** > **Service Accounts** > **Generate new private key**).
2. Simpan file `serviceAccountKey.json` di folder `VPS` (sejajar dengan `server.js` dan `fcm_service.js`).
3. Pastikan dependensi terpasang: `npm install firebase-admin`.
4. Restart service: `pm2 restart server.js` (atau service manager yang Anda gunakan).
