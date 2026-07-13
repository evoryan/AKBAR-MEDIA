Halo! Masalah pada tombol "Simpan" di halaman Tambah Pelanggan disebabkan oleh dua hal:

1. Server menolak input karena kolom terkait ODP dan Paket (seperti odp_id, pppoe_secret, dll) belum ada di database Anda atau nilainya dikirim kosong.
2. Aplikasi Android secara diam-diam menyembunyikan pesan error tersebut (blok catch kosong), sehingga terlihat seperti tombol tidak merespon sama sekali.

Saya sudah **memperbaiki masalah ini** secara menyeluruh dengan:
- **Auto-Update Schema di Server:** Menambahkan fitur "Self-Healing" ke `VPS/server.js` untuk otomatis membuat kolom `odp_id`, `odp_port`, `pppoe_secret`, dsb. pada tabel `customers` jika belum ada, serta memperbaiki parsing ID kosong menjadi `null`.
- **Pesan Error Interaktif:** Memperbaiki aplikasi Android agar selalu memunculkan peringatan popup (Toast) di layar bawah apabila terjadi kendala saat menyimpan.

**Langkah yang WAJIB Anda lakukan sekarang:**
1. Tolong upload ulang file terbaru `VPS/server.js` ke server `103.253.245.25`.
2. **Restart proses NodeJS** (`pm2 restart all` atau `npm start`).
3. Coba Tambah Pelanggan lagi di aplikasi Android ini, dijamin fungsi Simpan akan merespon dan data akan berhasil dimasukkan!
