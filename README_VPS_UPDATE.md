# Update VPS Backend
Silakan unggah file `VPS/server.js` ke server VPS Anda dan restart service (misalnya menggunakan PM2: `pm2 restart server.js` atau systemctl).

Skrip akan secara otomatis menambahkan kolom `area_id` pada tabel database sehingga dropdown menu yang baru dibuat dapat berfungsi.
