let makeWASocket, useMultiFileAuthState, DisconnectReason, pino, qrcode;
const fs = require('fs');
const path = require('path');
const express = require('express');

let isWhatsappSupported = false;

try {
    const baileys = require('@whiskeysockets/baileys');
    makeWASocket = baileys.default;
    useMultiFileAuthState = baileys.useMultiFileAuthState;
    DisconnectReason = baileys.DisconnectReason;
    
    pino = require('pino');
    qrcode = require('qrcode');
    isWhatsappSupported = true;
    console.log('[WA Gateway] Dependencies loaded successfully.');
} catch (err) {
    console.warn('\n========================================================================');
    console.warn('[WA Gateway] WARNING: WhatsApp modules are not fully installed on this VPS!');
    console.warn('[WA Gateway] Silakan jalankan perintah ini di terminal VPS Anda:');
    console.warn('             npm install @whiskeysockets/baileys pino qrcode');
    console.warn('========================================================================\n');
}

const router = express.Router();

// Global memory for tenant sessions
const sessions = {};

// We need a way to update the database state
let masterPool = null;
let getTenantPoolFn = null;

function setMasterPool(pool) {
    masterPool = pool;
}

function setGetTenantPool(fn) {
    getTenantPoolFn = fn;
}

async function logMessageHistory(tenantId, phone, message, status, mediaUrl = null) {
    if (!masterPool || !getTenantPoolFn) return;
    try {
        const [users] = await masterPool.query('SELECT db_name FROM users WHERE id = ?', [tenantId]);
        if (users.length > 0) {
            const dbName = users[0].db_name;
            const pool = getTenantPoolFn(dbName);
            if (pool) {
                // Ensure table exists
                await pool.query(`CREATE TABLE IF NOT EXISTS wa_history (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    customer_name VARCHAR(100),
                    phone VARCHAR(20),
                    message TEXT,
                    media_url VARCHAR(255),
                    status VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )`).catch(e => {});

                // Insert log
                await pool.query(
                    'INSERT INTO wa_history (customer_name, phone, message, media_url, status) VALUES (?, ?, ?, ?, ?)',
                    ['Uji Coba Kirim', phone, message, mediaUrl, status]
                );
                console.log(`[WA Gateway] Logged message to ${phone} with status ${status} in db ${dbName}`);
            }
        }
    } catch (err) {
        console.error(`[WA Gateway] Error logging message history for tenant ${tenantId}:`, err.message);
    }
}

async function updateTenantWaStatus(tenantId, status, botNumber = null) {
    if (!masterPool) return;
    try {
        await masterPool.query(
            `INSERT INTO tenant_whatsapp_sessions (tenant_id, status, bot_number) 
             VALUES (?, ?, ?) 
             ON DUPLICATE KEY UPDATE status = ?, bot_number = ?`,
            [tenantId, status, botNumber, status, botNumber]
        );
    } catch (err) {
        console.error(`[WA Gateway] Error updating DB for tenant ${tenantId}:`, err.message);
    }
}

async function initTenantWhatsApp(tenantId) {
    if (!isWhatsappSupported) {
        console.warn(`[WA Gateway] Cannot init WhatsApp for tenant ${tenantId}: missing dependencies.`);
        return;
    }

    // Prevent duplicate active connections/sessions
    if (sessions[tenantId]) {
        if (sessions[tenantId].status === 'connecting' || sessions[tenantId].status === 'qr' || sessions[tenantId].status === 'connected') {
            console.log(`[WA Gateway] Tenant ${tenantId} is already active/connecting (status: ${sessions[tenantId].status}). Skipping duplicate init.`);
            return;
        }
    }

    const sessionDir = path.join(__dirname, `whatsapp_sessions/tenant_${tenantId}`);
    
    // Ensure session directory exists
    if (!fs.existsSync(sessionDir)) {
        fs.mkdirSync(sessionDir, { recursive: true });
    }

    // Process file locking to prevent duplicate connections across multiple Node processes (e.g. PM2 Clusters)
    const lockFilePath = path.join(sessionDir, 'session.lock');
    if (fs.existsSync(lockFilePath)) {
        try {
            const lockPid = parseInt(fs.readFileSync(lockFilePath, 'utf8').trim(), 10);
            if (lockPid && lockPid !== process.pid) {
                try {
                    // Check if the process holding the lock is still alive
                    process.kill(lockPid, 0);
                    console.log(`[WA Gateway] Tenant ${tenantId} socket is already active in another process (PID: ${lockPid}). Skipping duplicate init.`);
                    return;
                } catch (err) {
                    // Process is dead, lock is stale, clean it up
                    console.log(`[WA Gateway] Stale lock file found for tenant ${tenantId} (PID ${lockPid} is dead). Removing lock.`);
                    try { fs.unlinkSync(lockFilePath); } catch (e) {}
                }
            }
        } catch (err) {
            console.warn(`[WA Gateway] Failed to verify lock file for tenant ${tenantId}:`, err.message);
        }
    }

    // Acquire lock for this process
    try {
        fs.writeFileSync(lockFilePath, process.pid.toString(), 'utf8');
    } catch (err) {
        console.error(`[WA Gateway] Failed to write lock file for tenant ${tenantId}:`, err.message);
    }

    // Safely clear old references, sockets, and timeouts before initiating a new one
    if (sessions[tenantId]) {
        if (sessions[tenantId].reconnectTimeout) {
            clearTimeout(sessions[tenantId].reconnectTimeout);
            sessions[tenantId].reconnectTimeout = null;
        }
        if (sessions[tenantId].sock) {
            try {
                sessions[tenantId].sock.ev.removeAllListeners('connection.update');
                sessions[tenantId].sock.ev.removeAllListeners('creds.update');
                sessions[tenantId].sock.end();
            } catch (err) {
                console.warn(`[WA Gateway] Error ending old socket for tenant ${tenantId}:`, err.message);
            }
            sessions[tenantId].sock = null;
        }
        sessions[tenantId].status = 'connecting';
    } else {
        sessions[tenantId] = {
            sock: null,
            status: 'connecting', // 'disconnected', 'connecting', 'qr', 'connected'
            qr: null,
            qrImage: null,
            botNumber: null,
            reconnectTimeout: null
        };
    }

    await updateTenantWaStatus(tenantId, 'CONNECTING');

    console.log(`[WA Gateway] Initializing socket for tenant: ${tenantId}`);

    try {
        const { state, saveCreds } = await useMultiFileAuthState(sessionDir);

        const sock = makeWASocket({
            logger: pino({ level: 'silent' }),
            printQRInTerminal: false,
            auth: state,
            browser: [`Akbar Media - Tenant ${tenantId}`, 'Chrome', '1.0.0']
        });

        sessions[tenantId].sock = sock;

        sock.ev.on('connection.update', async (update) => {
            const { connection, lastDisconnect, qr } = update;
            
            if (qr) {
                sessions[tenantId].qr = qr;
                sessions[tenantId].status = 'qr';
                try {
                    sessions[tenantId].qrImage = await qrcode.toDataURL(qr);
                } catch (err) {
                    console.error(`[WA Gateway] Failed to generate QR image for tenant ${tenantId}:`, err.message);
                }
                console.log(`[WA Gateway] New QR Code generated for tenant ${tenantId}.`);
                await updateTenantWaStatus(tenantId, 'PAIRING');
            }

            if (connection === 'close') {
                sessions[tenantId].qr = null;
                sessions[tenantId].qrImage = null;
                sessions[tenantId].botNumber = null;
                
                const shouldReconnect = lastDisconnect?.error?.output?.statusCode !== DisconnectReason.loggedOut;
                console.log(`[WA Gateway] Connection closed for tenant ${tenantId}. Reason:`, lastDisconnect?.error?.message, 'Reconnecting:', shouldReconnect);
                
                sessions[tenantId].status = 'disconnected';
                await updateTenantWaStatus(tenantId, 'DISCONNECTED');
                
                if (shouldReconnect) {
                    if (sessions[tenantId].reconnectTimeout) {
                        clearTimeout(sessions[tenantId].reconnectTimeout);
                    }
                    sessions[tenantId].reconnectTimeout = setTimeout(() => {
                        sessions[tenantId].reconnectTimeout = null;
                        initTenantWhatsApp(tenantId);
                    }, 5000);
                } else {
                    console.log(`[WA Gateway] Tenant ${tenantId} logged out. Clearing session directory...`);
                    cleanTenantSession(tenantId);
                }
            } else if (connection === 'open') {
                console.log(`[WA Gateway] WhatsApp connection opened for tenant ${tenantId}!`);
                sessions[tenantId].status = 'connected';
                sessions[tenantId].qr = null;
                sessions[tenantId].qrImage = null;
                
                // Extract bot phone number
                const user = sock.user;
                if (user && user.id) {
                    sessions[tenantId].botNumber = user.id.split(':')[0];
                    console.log(`[WA Gateway] Tenant ${tenantId} logged in as: ${sessions[tenantId].botNumber}`);
                    await updateTenantWaStatus(tenantId, 'CONNECTED', sessions[tenantId].botNumber);
                }
            }
        });

        sock.ev.on('creds.update', saveCreds);

    } catch (error) {
        console.error(`[WA Gateway] Error initializing socket for tenant ${tenantId}:`, error.message);
        sessions[tenantId].status = 'disconnected';
        await updateTenantWaStatus(tenantId, 'DISCONNECTED');
    }
}

function cleanTenantSession(tenantId) {
    const sessionDir = path.join(__dirname, `whatsapp_sessions/tenant_${tenantId}`);
    try {
        const lockFilePath = path.join(sessionDir, 'session.lock');
        if (fs.existsSync(lockFilePath)) {
            try {
                const lockPid = parseInt(fs.readFileSync(lockFilePath, 'utf8').trim(), 10);
                if (lockPid === process.pid) {
                    fs.unlinkSync(lockFilePath);
                    console.log(`[WA Gateway] Lock file removed for tenant ${tenantId}.`);
                }
            } catch (e) {}
        }
        if (fs.existsSync(sessionDir)) {
            fs.rmSync(sessionDir, { recursive: true, force: true });
            console.log(`[WA Gateway] Session directory deleted for tenant ${tenantId}.`);
        }
        if (sessions[tenantId]) {
            if (sessions[tenantId].reconnectTimeout) {
                clearTimeout(sessions[tenantId].reconnectTimeout);
                sessions[tenantId].reconnectTimeout = null;
            }
            if (sessions[tenantId].sock) {
                try {
                    sessions[tenantId].sock.ev.removeAllListeners('connection.update');
                    sessions[tenantId].sock.ev.removeAllListeners('creds.update');
                    sessions[tenantId].sock.end();
                } catch (e) {}
                sessions[tenantId].sock = null;
            }
            sessions[tenantId].status = 'disconnected';
            sessions[tenantId].botNumber = null;
            sessions[tenantId].qr = null;
            sessions[tenantId].qrImage = null;
        }
        updateTenantWaStatus(tenantId, 'DISCONNECTED');
    } catch (err) {
        console.error(`[WA Gateway] Error deleting session directory for tenant ${tenantId}:`, err.message);
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
async function sendTenantMessage(tenantId, to, message, mediaUrl = null) {
    const session = sessions[tenantId];
    if (!session || session.status !== 'connected' || !session.sock) {
        throw new Error(`WhatsApp gateway for tenant ${tenantId} is not connected!`);
    }
    try {
        const jid = formatWaNumber(to);
        let result;
        if (mediaUrl) {
            result = await session.sock.sendMessage(jid, { 
                image: { url: mediaUrl }, 
                caption: message 
            });
        } else {
            result = await session.sock.sendMessage(jid, { text: message });
        }
        return result;
    } catch (err) {
        console.error(`[WA Gateway] Send message failed for tenant ${tenantId}:`, err.message);
        throw err;
    }
}

// API Routes to be mounted at /api/tenant/whatsapp
router.get('/status/:tenant_id', (req, res) => {
    const tenantId = req.params.tenant_id;
    const session = sessions[tenantId];
    
    if (!session) {
        return res.json({ connected: false, status: 'disconnected', botNumber: null });
    }

    res.json({
        connected: session.status === 'connected',
        status: session.status,
        botNumber: session.botNumber
    });
});

router.get('/qr/:tenant_id', async (req, res) => {
    const tenantId = req.params.tenant_id;
    
    if (!sessions[tenantId] || sessions[tenantId].status === 'disconnected') {
        await initTenantWhatsApp(tenantId);
    }

    const session = sessions[tenantId];

    if (session.status === 'connected') {
        return res.json({ connected: true, message: 'WhatsApp already connected' });
    }
    
    res.json({
        connected: false,
        status: session.status,
        qr: session.qr,
        qrImage: session.qrImage
    });
});

router.post('/send/:tenant_id', async (req, res) => {
    const tenantId = req.params.tenant_id;
    const { phone, message } = req.body;
    
    if (!phone || !message) {
        return res.status(400).json({ error: 'Phone and message are required parameters' });
    }
    
    try {
        const result = await sendTenantMessage(tenantId, phone, message);
        await logMessageHistory(tenantId, phone, message, 'TERKIRIM');
        res.json({ status: 'success', info: 'Message sent successfully', result });
    } catch (err) {
        await logMessageHistory(tenantId, phone, message, 'GAGAL');
        res.status(500).json({ error: 'Failed to send message: ' + err.message });
    }
});

router.post('/disconnect/:tenant_id', async (req, res) => {
    const tenantId = req.params.tenant_id;
    try {
        const session = sessions[tenantId];
        if (session && session.sock) {
            await session.sock.logout();
            session.sock.end();
        }
        cleanTenantSession(tenantId);
        res.json({ status: 'success', message: 'WhatsApp logged out and session cleared' });
    } catch (err) {
        res.status(500).json({ error: 'Failed to disconnect: ' + err.message });
    }
});

// Automatically load all connected sessions from DB on startup
async function loadAllTenantSessions() {
    if (!masterPool) return;
    try {
        const [rows] = await masterPool.query('SELECT tenant_id FROM tenant_whatsapp_sessions WHERE status = "CONNECTED" OR status = "PAIRING" OR status = "CONNECTING"');
        for (const row of rows) {
            await initTenantWhatsApp(row.tenant_id);
        }
    } catch (err) {
        console.error('[WA Gateway] Failed to load tenant sessions:', err.message);
    }
}

module.exports = {
    router,
    initTenantWhatsApp,
    sendTenantMessage,
    setMasterPool,
    setGetTenantPool,
    loadAllTenantSessions,
    sessions
};
