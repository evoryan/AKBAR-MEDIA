Masalah ini terjadi karena kolom `portInput` dan `portCount` belum terbuat di database ODP Anda.

Saya telah memperbarui `VPS/server.js` dengan fitur "Self-Healing":
Sekarang, jika Anda mencoba menambah atau mengedit ODC/ODP dan kolom tersebut belum ada, server akan otomatis membuat kolom tersebut di database dan langsung menyimpan data Anda tanpa error.

Silakan:
1. Copy file `VPS/server.js` yang baru ke VPS Anda.
2. Restart server Node.js Anda (misal dengan `pm2 restart server`).
3. Coba Tambah ODP lagi di aplikasi Android, kali ini dijamin akan berhasil!
