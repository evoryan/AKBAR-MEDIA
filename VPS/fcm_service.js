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
 * Aplikasi Android (Client) subscribe ke topik "tenant_{dbName}"
 * 
 * @param {string} tenantDbName Nama database tenant untuk isolasi
 * @param {string} title Judul notifikasi
 * @param {string} body Isi pesan notifikasi
 * @param {object} extraData Data tambahan untuk payload notifikasi
 */
async function sendTenantNotification(tenantDbName, title, body, extraData = {}) {
    if (!isInitialized) {
        console.log("[FCM] Firebase belum diinisialisasi atau serviceAccountKey.json tidak ada. Notifikasi dilewati.");
        return;
    }
    if (!tenantDbName || typeof tenantDbName !== 'string') {
        console.warn("[FCM] Notifikasi diabaikan: tenantDbName kosong atau tidak valid.");
        return;
    }

    // Bersihkan nama database agar valid untuk topic FCM (hanya huruf, angka, -, _, ~)
    const safeTopic = tenantDbName.replace(/[^a-zA-Z0-9-_~]/g, '');
    if (!safeTopic) {
        console.warn(`[FCM] Notifikasi diabaikan: nama topik kosong setelah dibersihkan dari "${tenantDbName}"`);
        return;
    }
    const topicName = `tenant_${safeTopic}`;

    const dataPayload = {
        title: String(title || ''),
        body: String(body || ''),
        db_name: String(tenantDbName),
        type: "payment",
        timestamp: String(Date.now()),
        ...Object.fromEntries(
            Object.entries(extraData || {}).map(([k, v]) => [String(k), String(v ?? '')])
        )
    };

    const message = {
        notification: {
            title: title,
            body: body
        },
        data: dataPayload,
        android: {
            priority: 'high',
            notification: {
                channelId: 'billing_notifications',
                sound: 'default',
                priority: 'high'
            }
        },
        topic: topicName
    };

    try {
        const response = await admin.messaging().send(message);
        console.log(`[FCM] Notifikasi pembayaran berhasil dikirim terisolasi ke database [${tenantDbName}] (topik: ${topicName}):`, response);
    } catch (error) {
        console.error(`[FCM] Gagal mengirim notifikasi ke database [${tenantDbName}] (topik: ${topicName}):`, error.message);
    }
}

module.exports = {
    sendTenantNotification
};
