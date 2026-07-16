# Panduan Instalasi Backend VPS

Backend ini dibangun menggunakan Node.js dan MySQL (atau MariaDB).

## Persyaratan
- Node.js (Versi 16 atau yang lebih baru)
- MySQL atau MariaDB
- PM2 (Opsional, disarankan agar aplikasi tetap berjalan di latar belakang)

## Langkah Instalasi

1. **Install Node.js & NPM** (jika belum):
   ```bash
   sudo apt update
   sudo apt install nodejs npm
   ```

2. **Install MySQL** (jika belum):
   ```bash
   sudo apt install mysql-server
   ```

3. **Import Database**:
   - Silakan login ke MySQL Anda: `mysql -u root -p`
   - Buat database atau langsung gunakan file `init.sql`:
   ```bash
   mysql -u root -p < init.sql
   ```
   (Ini akan otomatis membuat database `app_db` beserta seluruh tabel dan contoh datanya).

4. **Konfigurasi Environment**:
   - Copy file `.env.example` menjadi `.env`
   ```bash
   cp .env.example .env
   ```
   - Sesuaikan konfigurasi database (`DB_USER`, `DB_PASSWORD`, `DB_NAME`) di dalam file `.env` dengan kredensial MySQL Anda.

5. **Install Dependencies**:
   Jalankan perintah ini di dalam direktori VPS untuk menginstal semua library yang dibutuhkan:
   ```bash
   npm install
   ```

6. **Menjalankan Server (Mode Development)**:
   ```bash
   npm run dev
   ```

7. **Menjalankan Server secara Permanen (Production)**:
   Sangat disarankan menggunakan **PM2**:
   ```bash
   sudo npm install -g pm2
   pm2 start server.js --name "vps-backend"
   pm2 save
   pm2 startup
   ```

Server akan berjalan pada port **4500** secara default. Pastikan port 4500 telah diizinkan pada firewall (UFW/Iptables) di VPS Anda.

---
**Catatan**: IP pada aplikasi Android `ApiClient.kt` telah diset mengarah ke port 4500. Jika Anda mengganti IP VPS, pastikan juga memperbarui `BASE_URL` di `ApiClient.kt` lalu build ulang APK Android-nya.
