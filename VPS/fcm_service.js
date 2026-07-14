const admin = require('firebase-admin');

// Inisialisasi Firebase Admin
// Anda harus mengunduh file 'serviceAccountKey.json' dari Firebase Console
// (Project Settings -> Service Accounts -> Generate New Private Key)
// dan menyimpannya di folder yang sama dengan file ini.
let isInitialized = false;

try {
    const serviceAccount = require('./serviceAccountKey.json');
    admin.initializeApp({
        credential: admin.credential.cert(serviceAccount)
    });
    isInitialized = true;
    console.log("Firebase Admin SDK berhasil diinisialisasi.");
} catch (error) {
    console.log("Warning: Firebase serviceAccountKey.json tidak ditemukan atau tidak valid. FCM dinonaktifkan.");
}

/**
 * Mengirim notifikasi push terisolasi ke topik tenant tertentu.
 * Aplikasi Android (Client) harus subscribe ke topik "tenant_{dbName}"
 * 
 * @param {string} tenantDbName Nama database tenant untuk isolasi
 * @param {string} title Judul notifikasi
 * @param {string} body Isi pesan notifikasi
 */
async function sendTenantNotification(tenantDbName, title, body) {
    if (!isInitialized) return;

    // Bersihkan nama database agar valid untuk topic FCM (hanya huruf, angka, -, _, ~)
    const safeTopic = tenantDbName.replace(/[^a-zA-Z0-9-_~]/g, '');
    const topicName = `tenant_${safeTopic}`;

    const message = {
        notification: {
            title: title,
            body: body
        },
        topic: topicName
    };

    try {
        const response = await admin.messaging().send(message);
        console.log(`[FCM] Notifikasi berhasil dikirim ke topik ${topicName}:`, response);
    } catch (error) {
        console.error(`[FCM] Gagal mengirim notifikasi ke topik ${topicName}:`, error.message);
    }
}

module.exports = {
    sendTenantNotification
};
