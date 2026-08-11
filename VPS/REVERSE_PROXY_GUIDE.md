# PANDUAN KONFIGURASI DOMAIN & SSL (REVERSE PROXY NGINX) - AKBAR MEDIA

Dokumen ini menjelaskan langkah-langkah lengkap untuk menghubungkan domain **https://amg.akbarmediagroup.me** ke aplikasi web Node.js yang berjalan pada port **4100** (`http://103.253.245.25:4100`) menggunakan **Nginx** sebagai Reverse Proxy dan **Certbot (Let's Encrypt)** untuk SSL gratis.

---

## RINGKASAN ALUR KONEKSI
```
[User Browser] -> https://amg.akbarmediagroup.me (Port 443 SSL)
                     ↓
             [Nginx Web Server] (Mendekripsi SSL & meneruskan request)
                     ↓
             [Node.js Web App] -> http://localhost:4100 (Internal)
```

---

## LANGKAH 1: KONFIGURASI DNS (CLOUDFLARE / DOMAIN PROVIDER)
Sebelum melakukan konfigurasi di VPS, Anda harus mengarahkan domain/subdomain Anda ke IP VPS:

1. Masuk ke dashboard penyedia domain Anda (misal: Cloudflare, Niagahoster, Rumahweb, dll.).
2. Masuk ke menu **DNS Management** untuk domain `akbarmediagroup.me`.
3. Tambahkan **A Record** baru dengan detail berikut:
   * **Type**: `A`
   * **Name / Host**: `amg` (untuk membuat `amg.akbarmediagroup.me`)
   * **Value / IPv4 Address**: `103.253.245.25`
   * **TTL**: `Auto` atau `3600`
   * **Proxy Status** (Jika menggunakan Cloudflare): Matikan/bypass terlebih dahulu (berwarna abu-abu / *DNS Only*) agar proses verifikasi SSL Certbot berjalan lancar. Setelah SSL terpasang, Anda bisa mengaktifkan proxy Cloudflare kembali jika diinginkan.

> *Catatan: Rambatan DNS (DNS Propagation) biasanya membutuhkan waktu antara 5 menit hingga beberapa jam.*

---

## LANGKAH 2: INSTALL NGINX DI VPS
Hubungkan SSH ke VPS Anda (`ssh root@103.253.245.25`) dan jalankan perintah berikut:

```bash
# Update package list
sudo apt update

# Install Nginx
sudo apt install nginx -y

# Pastikan Nginx berjalan dan otomatis menyala saat server reboot
sudo systemctl start nginx
sudo systemctl enable nginx
```

---

## LANGKAH 3: BUAT KONFIGURASI NGINX UNTUK WEB PORTAL
Buat file konfigurasi baru di Nginx untuk mengarahkan traffic domain ke port `4100` lewat port `81` (karena port `80` sudah digunakan oleh layanan lain).

1. Buat file konfigurasinya:
   ```bash
   sudo nano /etc/nginx/sites-available/amg.akbarmediagroup.me
   ```

2. Tempelkan (*paste*) konfigurasi berikut ke dalam file tersebut (menggunakan port **81**):
   ```nginx
   server {
       listen 81;
       server_name amg.akbarmediagroup.me;

       # Mengatur batas upload jika aplikasi membutuhkan upload file besar
       client_max_body_size 50M;

       # [1] FRONTEND PORTAL (Port 4100)
       location / {
           proxy_pass http://127.0.0.1:4100;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }

       # [2] BACKEND API (Port 4500)
       location /api {
           proxy_pass http://127.0.0.1:4500;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection 'upgrade';
           proxy_set_header Host $host;
           proxy_cache_bypass $http_upgrade;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }

       # [3] BACKEND UPLOADS STATIC (Port 4500)
       location /uploads {
           proxy_pass http://127.0.0.1:4500/uploads;
           proxy_http_version 1.1;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
   }
   ```

3. Simpan file tersebut (Tekan `Ctrl + O` lalu `Enter`, kemudian keluar dengan `Ctrl + X`).

4. Aktifkan konfigurasi dengan membuat *symbolic link* ke folder `sites-enabled`:
   ```bash
   sudo ln -s /etc/nginx/sites-available/amg.akbarmediagroup.me /etc/nginx/sites-enabled/
   ```

5. Uji apakah konfigurasi Nginx sudah benar dan tidak ada typo/error:
   ```bash
   sudo nginx -t
   ```
   *Jika sukses, Anda akan melihat pesan: `nginx: configuration file ... test is successful`*

6. Restart Nginx untuk menerapkan perubahan:
   ```bash
   sudo systemctl restart nginx
   ```

---

## LANGKAH 4: PASANG SSL GRATIS (HTTPS) DENGAN CERTBOT (MENGGUNAKAN PORT 81)
Karena port `80` sudah dipakai oleh layanan lain, proses verifikasi HTTP-01 default Certbot (yang biasanya berjalan di port 80) perlu disesuaikan. Kita bisa memerintahkan Certbot untuk melakukan tantangan/challenge verifikasi melalui port `81` yang sudah kita konfigurasikan di Nginx.

1. Install Certbot dan plugin Nginx (jika belum):
   ```bash
   sudo apt install certbot python3-certbot-nginx -y
   ```

2. Jalankan Certbot dengan menyertakan port alternatif **81** untuk verifikasi HTTP-01:
   ```bash
   sudo certbot --nginx -d amg.akbarmediagroup.me --http-01-port 81
   ```

3. Certbot akan mendeteksi server block Nginx untuk `amg.akbarmediagroup.me` yang berjalan di port `81`, melakukan verifikasi, dan memasang SSL otomatis untuk port HTTPS `443` (yang aman dan terenkripsi).

4. Certbot akan meminta beberapa informasi:
   * **Email**: Masukkan email Anda (misal: `satriaevo77@gmail.com`).
   * **Terms of Service**: Tekan `A` untuk menyetujui (*Agree*).
   * **Share Email**: Tekan `N` jika tidak ingin berlangganan newsletter Let's Encrypt.
   * **Redirect**: Jika ditanya apakah ingin mengarahkan HTTP ke HTTPS secara otomatis (*Redirect all HTTP traffic to HTTPS*), pilih opsi **2** (Redirect / Secure).

5. Setelah selesai, Certbot akan membuat server block baru yang mendengarkan port **443** (SSL) dan mengarahkan semua akses HTTP dari port **81** langsung ke HTTPS **443**.

6. Restart Nginx sekali lagi agar sertifikat SSL aktif:
   ```bash
   sudo systemctl restart nginx
   ```

---

## LANGKAH 5: PASTIKAN PORT DI VPS SUDAH DIIZINKAN (FIREWALL)
Jika domain Anda masih tidak bisa diakses, pastikan port `81` (HTTP alternatif) dan `443` (HTTPS) tidak diblokir oleh firewall VPS (seperti UFW):

```bash
# Izinkan Nginx Full (Port 81 & 443) di firewall UFW
sudo ufw allow 81/tcp
sudo ufw allow 443/tcp
sudo ufw allow 'Nginx Full'

# Atau jika menggunakan iptables/provider seperti AWS/Alibaba/Google Cloud, pastikan Security Group membuka Port:
# - Port 81 (HTTP Alternatif)
# - Port 443 (HTTPS)
```

---

## TIPS PERBAIKAN UMUM (TROUBLESHOOTING)
* **Error 502 Bad Gateway**: Berarti Nginx aktif, tetapi aplikasi Node.js Anda di port `4100` sedang mati atau belum dijalankan. Pastikan Anda menjalankan aplikasi web Anda di VPS menggunakan PM2 agar tetap hidup di background:
  ```bash
  # Menjalankan aplikasi web dengan PM2 agar selalu online
  cd /path/ke/folder/WEB
  pm2 start server.js --name "akbar-media-web"
  pm2 save
  ```
* **Koneksi Selalu Timeout**: Periksa kembali konfigurasi DNS A Record Anda di Cloudflare atau registrar domain Anda dan pastikan IP yang dimasukkan sudah benar-benar sesuai (`103.253.245.25`).
* **Sertifikat SSL Expired**: Certbot Let's Encrypt otomatis memperbarui sertifikat setiap 90 hari. Anda bisa menguji auto-renewal dengan perintah: `sudo certbot renew --dry-run`.
