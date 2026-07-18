const { default: makeWASocket, useMultiFileAuthState, DisconnectReason } = require('@whiskeysockets/baileys');
const pino = require('pino');
const fs = require('fs');
const path = require('path');
const qrcode = require('qrcode');
const express = require('express');

const router = express.Router();

let sock = null;
let connectionStatus = 'disconnected'; // 'disconnected', 'connecting', 'qr', 'connected'
let latestQr = null;
let latestQrImage = null;
let myNumber = null;

const sessionDir = path.join(__dirname, 'auth_info_baileys');

async function initWhatsApp() {
    console.log('[WA Gateway] Initializing WhatsApp Socket...');
    connectionStatus = 'connecting';
    
    // Ensure session directory exists
    if (!fs.existsSync(sessionDir)) {
        fs.mkdirSync(sessionDir, { recursive: true });
    }

    try {
        const { state, saveCreds } = await useMultiFileAuthState(sessionDir);

        sock = makeWASocket({
            logger: pino({ level: 'silent' }),
            printQRInTerminal: true,
            auth: state,
            browser: ['Akbar Media Bot', 'Chrome', '1.0.0']
        });

        sock.ev.on('connection.update', async (update) => {
            const { connection, lastDisconnect, qr } = update;
            
            if (qr) {
                latestQr = qr;
                connectionStatus = 'qr';
                try {
                    latestQrImage = await qrcode.toDataURL(qr);
                } catch (err) {
                    console.error('[WA Gateway] Failed to generate QR image:', err.message);
                }
                console.log('[WA Gateway] New QR Code generated. Ready to scan.');
            }

            if (connection === 'close') {
                latestQr = null;
                latestQrImage = null;
                myNumber = null;
                
                const shouldReconnect = lastDisconnect?.error?.output?.statusCode !== DisconnectReason.loggedOut;
                console.log('[WA Gateway] Connection closed. Reason:', lastDisconnect?.error?.message, 'Reconnecting:', shouldReconnect);
                
                connectionStatus = 'disconnected';
                if (shouldReconnect) {
                    // Reconnect after delay
                    setTimeout(initWhatsApp, 5000);
                } else {
                    console.log('[WA Gateway] Logged out. Clearing session directory...');
                    cleanSession();
                }
            } else if (connection === 'open') {
                console.log('[WA Gateway] WhatsApp connection opened successfully!');
                connectionStatus = 'connected';
                latestQr = null;
                latestQrImage = null;
                
                // Extract bot phone number
                const user = sock.user;
                if (user && user.id) {
                    myNumber = user.id.split(':')[0];
                    console.log(`[WA Gateway] Logged in as: ${myNumber}`);
                }
            }
        });

        sock.ev.on('creds.update', saveCreds);

    } catch (error) {
        console.error('[WA Gateway] Error initializing socket:', error.message);
        connectionStatus = 'disconnected';
    }
}

function cleanSession() {
    try {
        if (fs.existsSync(sessionDir)) {
            fs.rmSync(sessionDir, { recursive: true, force: true });
            console.log('[WA Gateway] Session directory deleted.');
        }
    } catch (err) {
        console.error('[WA Gateway] Error deleting session directory:', err.message);
    }
}

// Format phone numbers to JID
function formatWaNumber(phone) {
    let clean = phone.replace(/[^0-9]/g, '');
    if (clean.startsWith('0')) {
        clean = '62' + clean.slice(1);
    }
    if (!clean.endsWith('@s.whatsapp.net')) {
        clean = clean + '@s.whatsapp.net';
    }
    return clean;
}

// Function to send message manually
async function sendMessage(to, message) {
    if (connectionStatus !== 'connected' || !sock) {
        throw new Error('WhatsApp gateway is not connected!');
    }
    try {
        const jid = formatWaNumber(to);
        const result = await sock.sendMessage(jid, { text: message });
        return result;
    } catch (err) {
        console.error('[WA Gateway] Send message failed:', err.message);
        throw err;
    }
}

// Express API Routes
router.get('/status', (req, res) => {
    res.json({
        connected: connectionStatus === 'connected',
        status: connectionStatus,
        botNumber: myNumber,
        sessionPath: sessionDir
    });
});

router.get('/qr', (req, res) => {
    if (connectionStatus === 'connected') {
        return res.json({ connected: true, message: 'WhatsApp already connected' });
    }
    res.json({
        connected: false,
        status: connectionStatus,
        qr: latestQr,
        qrImage: latestQrImage
    });
});

router.post('/send', async (req, res) => {
    const { phone, message } = req.body;
    if (!phone || !message) {
        return res.status(400).json({ error: 'Phone and message are required parameters' });
    }
    
    try {
        const result = await sendMessage(phone, message);
        res.json({ status: 'success', info: 'Message sent successfully', result });
    } catch (err) {
        res.status(500).json({ error: 'Failed to send message: ' + err.message });
    }
});

router.post('/disconnect', async (req, res) => {
    try {
        if (sock) {
            await sock.logout();
            sock.end();
        }
        cleanSession();
        connectionStatus = 'disconnected';
        latestQr = null;
        latestQrImage = null;
        myNumber = null;
        res.json({ status: 'success', message: 'WhatsApp logged out and session cleared' });
    } catch (err) {
        res.status(500).json({ error: 'Failed to disconnect: ' + err.message });
    }
});

module.exports = {
    router,
    initWhatsApp,
    sendMessage,
    getStatus: () => connectionStatus,
    getBotNumber: () => myNumber
};
