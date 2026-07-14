# Panduan Firebase Cloud Messaging (FCM) di VPS

Fitur Push Notification terisolasi antar tenant telah ditambahkan ke sistem backend Anda.
Setiap kali ada pembayaran masuk, notifikasi akan dikirim secara real-time ke aplikasi Android.

## Persyaratan
Untuk dapat menggunakan fitur ini, Anda harus membuat proyek di Firebase dan mengunduh kunci `serviceAccountKey.json`.

## Langkah-langkah Setup Firebase Admin (Server)

1. Buka [Firebase Console](https://console.firebase.google.com/) dan buat proyek baru (atau gunakan proyek yang sudah ada).
2. Pergi ke **Project Settings** (ikon gir) > **Service Accounts**.
3. Di bawah tulisan **Firebase Admin SDK**, klik tombol **Generate new private key** (Hasilkan kunci privat baru).
4. Sebuah file JSON akan diunduh. Ganti nama file tersebut menjadi `serviceAccountKey.json`.
5. Pindahkan file `serviceAccountKey.json` ke dalam folder `VPS` (sejajar dengan file `server.js` dan `fcm_service.js`).
6. Di terminal VPS Anda, instal library Firebase Admin dengan menjalankan:
   `npm install firebase-admin` (Ini sudah ditambahkan di package.json).
7. Restart service backend Anda (misal: `pm2 restart server.js`).

**Catatan**: Jika file `serviceAccountKey.json` tidak ada, server tidak akan error, tapi akan mengeluarkan peringatan di log dan fitur FCM dinonaktifkan.

## Penyesuaian Aplikasi Android (Klien)

Notifikasi ini menggunakan sistem topik. Topik diisolasi per database tenant.

Di aplikasi Android Anda, Anda perlu menambahkan kodingan untuk subscribe ke topik Firebase setelah admin berhasil login ke tenant tertentu.
Contoh:
```kotlin
val topicName = "tenant_" + dbName.replace("[^a-zA-Z0-9-_~]".toRegex(), "")
FirebaseMessaging.getInstance().subscribeToTopic(topicName)
    .addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("FCM", "Berhasil subscribe ke topik $topicName")
        }
    }
```
Pastikan Anda sudah mengimplementasikan Firebase Cloud Messaging di Android (`google-services.json` dan dependency `com.google.firebase:firebase-messaging`).
