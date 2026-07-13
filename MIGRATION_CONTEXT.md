# Konteks Migrasi ke Web (React/Next.js/dsb)

Dokumen ini berisi rangkuman arsitektur backend yang digunakan pada proyek Android sebelumnya, untuk digunakan sebagai referensi saat membangun versi Web dari aplikasi ini.

## 1. Arsitektur Multi-Tenant
Aplikasi ini menggunakan arsitektur **Database-per-Tenant**.
- **Master Database**: `akbar_media_master`
- Tabel `users` di master database menyimpan semua akun Super Admin/Admin beserta informasi `db_name` (nama database tenant mereka).
- Saat user login di `/api/login`, backend membaca `akbar_media_master.users`, memvalidasi password, lalu mengembalikan **JWT Token** yang di dalamnya di-*embed* informasi `db_name`.

## 2. JWT & Middleware
- Token ditandatangani menggunakan `JWT_SECRET`.
- Pada setiap *request* setelah login, client wajib mengirimkan header: `Authorization: Bearer <token>`.
- Middleware `tenantContext` di backend akan men-*decode* token tersebut, mengambil `db_name`, lalu menghubungkan *request* ke pool MySQL (secara dinamis) khusus untuk `db_name` tersebut. Hal ini memastikan data antar-tenant tidak akan bocor/tercampur.

## 3. Struktur Folder Backend (VPS)
Folder `VPS/` berisi seluruh kode backend Node.js (Express) yang dibutuhkan:
- `server.js`: File utama yang memuat endpoint API (termasuk Mikrotik ACS proxy), koneksi MySQL, dan autentikasi JWT.
- `init.sql`: Script SQL untuk menginisiasi tabel-tabel di dalam database tenant baru.
- `init_master.sql`: Script SQL untuk menginisiasi database master dan tabel `users`.
- `add_tenant.js`: Script Node.js untuk membuat tenant baru via terminal (membuat DB baru, meng-*import* `init.sql`, dan menambahkan superadmin ke database master).

## 4. Cara Penggunaan di Proyek Baru
1. Buat proyek AI Studio baru (misal: "Web Frontend React").
2. *Upload* atau berikan folder `VPS/` kepada agen AI di proyek baru sebagai referensi API/Backend yang sudah ada.
3. Minta agen AI untuk membuat aplikasi frontend Web (misalnya menggunakan React, Tailwind, Vite) yang berkomunikasi dengan *endpoint-endpoint* di `VPS/server.js`, dengan tetap menggunakan pola autentikasi Bearer Token (JWT).
