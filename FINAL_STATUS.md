Halo! Masalah pada form tambah ODC dan ODP yang tidak bisa menyimpan data kemungkinan besar disebabkan oleh **kolom database yang belum diupdate di server VPS live Anda**.

Sebelumnya, kita telah menambahkan fitur kolom `portCount` dan `portInput` pada ODC dan ODP. Jika kode Android mengirimkan data ini tetapi database MySQL di VPS lama Anda belum memiliki kolom-kolom tersebut, server akan mengalami *Error 500* secara diam-diam.

Saya sudah **memperbaiki masalah ini** secara menyeluruh dengan:
1. **Auto-Update Schema di Server:** Menambahkan logika di dalam `VPS/server.js` agar secara otomatis menjalankan `ALTER TABLE` pada saat server node di-restart. Fitur ini otomatis menambahkan kolom `portCount` dan `portInput` ke MySQL Anda bila belum ada.
2. **Validasi ODP:** Menambahkan pesan peringatan pada form ODP apabila belum ada ODC yang dipilih, sehingga mencegah error penyimpanan kosong.
3. **Pesan Error Interaktif:** Menambahkan notifikasi (`Toast`) pada aplikasi Android, sehingga apabila di masa depan terjadi kegagalan server, Anda akan langsung melihat peringatan (contoh: *Error: Timeout*) bukan layar yang terdiam begitu saja.

**Langkah yang WAJIB Anda lakukan sekarang:**
Tolong upload ulang semua perubahan kode terbaru yang ada di folder `VPS` ini (terutama file `server.js`) ke server `103.253.245.25`, dan **restart proses NodeJS** (`pm2 restart all` atau `npm start`). Setelah server VPS menyala kembali menggunakan `server.js` yang baru, aplikasi Android ini akan bisa menyimpan ODC dan ODP dengan lancar!
