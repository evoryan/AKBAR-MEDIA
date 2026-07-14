# Panduan Instalasi Backend VPS (WiFi Billing)

Skrip ini adalah sisi backend (Node.js + MySQL) yang dirancang untuk VPS Anda.
Sistem ini menangani:
1. Pembuatan tagihan bulanan otomatis berdasarkan tanggal `billingDate` pengguna (menggunakan cron job setiap hari pukul 00:01).
2. Sinkronisasi data ke `pembukuan` saat pembayaran dilakukan (Pemasukan) atau dibatalkan (Pengeluaran/Pengembalian).

## Persyaratan
- Node.js (versi 16 atau lebih baru direkomendasikan)
- MySQL / MariaDB Server

## Langkah-langkah Instalasi:

1. **Unduh direktori ini** ke VPS Anda (misalnya ke `/var/www/vps_backend`).

2. **Buat Database dan Tabel (Schema):**
   Masuk ke MySQL VPS Anda dan jalankan perintah yang ada di file `schema.sql`.
   ```bash
   mysql -u root -p wifi_billing < schema.sql
   ```

3. **Install Dependensi:**
   Buka terminal/SSH VPS Anda, masuk ke direktori `vps_backend`, dan jalankan:
   ```bash
   npm install
   ```

4. **Konfigurasi Database:**
   Buka file `server.js` dan sesuaikan bagian ini dengan kredensial MySQL Anda:
   ```javascript
   const dbConfig = {
       host: 'localhost', // atau IP database Anda
       user: 'root',      // user MySQL
       password: 'password', // password MySQL
       database: 'wifi_billing'
   };
   ```

5. **Jalankan Server:**
   Anda dapat menguji menjalankannya dengan:
   ```bash
   node server.js
   ```

   **Rekomendasi untuk Production:**
   Gunakan `pm2` agar aplikasi tetap berjalan di background dan otomatis restart ketika server reboot:
   ```bash
   npm install -g pm2
   pm2 start server.js --name "wifi_billing_backend"
   pm2 save
   pm2 startup
   ```

6. **Konfigurasi Firewall:**
   Pastikan port `3000` (atau port yang Anda ubah di script) terbuka di firewall VPS Anda agar aplikasi Android dapat mengaksesnya.

## Penjelasan Endpoint yang Dibuat:
- **Cron Job**: Berjalan otomatis memeriksa hari. Jika ada pelanggan dengan `billingDate` hari ini, akan di-generate tagihan ke tabel `tagihan_bulanan` dan status di-update menjadi `BELUM BAYAR`.
- **POST `/api/billing/pay`**: Mengubah tagihan jadi `LUNAS CASH` dan menyisipkan rekap dana ke tabel `pembukuan`.
- **POST `/api/billing/delete`**: Membatalkan pembayaran terakhir, merubah status kembali menjadi `BELUM BAYAR`, dan menyisipkan pengeluaran kompensasi di tabel `pembukuan`.
