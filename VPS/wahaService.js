const express = require('express');
const axios = require('axios');
const qrcode = require('qrcode');

const router = express.Router();
const WAHA_API_URL = 'http://103.253.245.25:2200';
const WAHA_API_KEY = 'akbarapikey2026';

// Create a custom axios instance for WAHA to avoid impacting global axios settings
const wahaApi = axios.create();

// Add interceptor to automatically append the API Key if defined
wahaApi.interceptors.request.use(config => {
    if (WAHA_API_KEY) {
        config.headers['X-Api-Key'] = WAHA_API_KEY;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

let masterPool = null;
let getTenantPoolFn = null;

function setMasterPool(pool) {
    masterPool = pool;
}

function setGetTenantPool(fn) {
    getTenantPoolFn = fn;
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
        console.error(`[WAHA Service] Error updating DB status for tenant ${tenantId}:`, err.message);
    }
}

async function logMessageHistory(tenantId, phone, message, status, mediaUrl = null) {
    if (!masterPool || !getTenantPoolFn) return;
    try {
        const [users] = await masterPool.query('SELECT db_name FROM users WHERE id = ?', [tenantId]);
        if (users.length > 0) {
            const dbName = users[0].db_name;
            const pool = getTenantPoolFn(dbName);
            if (pool) {
                await pool.query(`CREATE TABLE IF NOT EXISTS wa_history (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    customer_name VARCHAR(100),
                    phone VARCHAR(20),
                    message TEXT,
                    media_url VARCHAR(255),
                    status VARCHAR(50),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )`).catch(() => {});

                await pool.query(
                    'INSERT INTO wa_history (customer_name, phone, message, media_url, status) VALUES (?, ?, ?, ?, ?)',
                    ['Uji Coba Kirim', phone, message, mediaUrl, status]
                );
                console.log(`[WAHA Service] Logged message to ${phone} with status ${status} in db ${dbName}`);
            }
        }
    } catch (err) {
        console.error(`[WAHA Service] Error logging message history for tenant ${tenantId}:`, err.message);
    }
}

// 1. Buat fungsi startTenantSession(tenantId)
async function startTenantSession(tenantId) {
    const sessionName = `tenant_${tenantId}`;
    try {
        console.log(`[WAHA Service] Starting session for ${sessionName}...`);
        const response = await wahaApi.post(`${WAHA_API_URL}/api/sessions/start`, { name: sessionName }, { timeout: 10000 });
        await updateTenantWaStatus(tenantId, 'CONNECTING', null);
        return response.data;
    } catch (err) {
        console.error(`[WAHA Service] Failed to start session ${sessionName}:`, err.message);
        throw err;
    }
}

// 2. Buat fungsi getTenantQR(tenantId)
async function getTenantQR(tenantId) {
    const sessionName = `tenant_${tenantId}`;
    try {
        // Retrieve current status
        const statusData = await getTenantStatus(tenantId);
        if (statusData.connected) {
            return { connected: true, status: 'CONNECTED', qr: null, qrImage: null };
        }

        // Check if session exists in WAHA; if not, start it
        const response = await wahaApi.get(`${WAHA_API_URL}/api/sessions`, { timeout: 3000 });
        const sessionsList = response.data;
        const found = sessionsList.find(s => s.name === sessionName);

        if (!found || found.status === 'STOPPED' || found.status === 'FAILED') {
            await startTenantSession(tenantId);
            // wait a brief moment for startup
            await new Promise(resolve => setTimeout(resolve, 2000));
        }

        // Get QR text from WAHA
        let qrText = null;
        try {
            const qrResponse = await wahaApi.get(`${WAHA_API_URL}/api/${sessionName}/auth/qr?format=raw`, { timeout: 3000 });
            qrText = qrResponse.data.value || qrResponse.data.qr || qrResponse.data.raw || (typeof qrResponse.data === 'string' ? qrResponse.data : null);
        } catch (e) {
            try {
                const qrResponse2 = await wahaApi.get(`${WAHA_API_URL}/api/${sessionName}/auth/qr`, { timeout: 3000 });
                qrText = qrResponse2.data.value || qrResponse2.data.qr || qrResponse2.data.raw || (typeof qrResponse2.data === 'string' ? qrResponse2.data : null);
            } catch (e2) {}
        }

        if (!qrText) {
            return { connected: false, status: 'CONNECTING', qr: null, qrImage: null };
        }

        // Convert raw text into Base64 PNG using qrcode package with higher resolution (512x512) for clear scanning
        const qrImage = await qrcode.toDataURL(qrText, { width: 512, margin: 2 });
        return {
            connected: false,
            status: 'CONNECTING',
            qr: qrText,
            qrImage: qrImage
        };
    } catch (err) {
        console.error(`[WAHA Service] Error getting QR for tenant ${tenantId}:`, err.message);
        return { connected: false, status: 'DISCONNECTED', qr: null, qrImage: null };
    }
}

// 3. Buat fungsi sendWhatsAppMessage(tenantId, to, text)
async function sendWhatsAppMessage(tenantId, to, text) {
    const sessionName = `tenant_${tenantId}`;
    let cleanPhone = to.replace(/\D/g, '');
    if (cleanPhone.startsWith('0')) {
        cleanPhone = '62' + cleanPhone.slice(1);
    }
    if (!cleanPhone.endsWith('@c.us')) {
        cleanPhone = cleanPhone + '@c.us';
    }

    const payload = {
        chatId: cleanPhone,
        text: text,
        session: sessionName
    };

    // Ensure session is started if stopped/failed/non-existent
    try {
        const response = await wahaApi.get(`${WAHA_API_URL}/api/sessions`, { timeout: 3000 });
        const sessionsList = response.data;
        const found = sessionsList.find(s => s.name === sessionName);

        if (!found || found.status === 'STOPPED' || found.status === 'FAILED') {
            console.log(`[WAHA Service] Session ${sessionName} is stopped or not found. Automatically starting...`);
            await startTenantSession(tenantId);
            await new Promise(resolve => setTimeout(resolve, 3000));
        }
    } catch (sessionCheckErr) {
        console.warn(`[WAHA Service] Pre-send session check error for ${sessionName}:`, sessionCheckErr.message);
    }

    try {
        const response = await wahaApi.post(`${WAHA_API_URL}/api/sendText`, payload, { timeout: 10000 });
        return response.data;
    } catch (err) {
        console.error(`[WAHA Service] Failed to send text message via session ${sessionName}:`, err.message);
        
        // Retry logic on closed/stopped session
        const errMsg = err.message || '';
        const responseData = err.response && err.response.data && JSON.stringify(err.response.data);
        const combinedError = (errMsg + ' ' + (responseData || '')).toLowerCase();
        
        if (combinedError.includes('session') && (combinedError.includes('not running') || combinedError.includes('closed') || combinedError.includes('not active') || combinedError.includes('not found') || combinedError.includes('failed'))) {
            try {
                console.log(`[WAHA Service] Retrying sendWhatsAppMessage after auto-starting session ${sessionName}...`);
                await startTenantSession(tenantId);
                await new Promise(resolve => setTimeout(resolve, 5000));
                
                const response = await wahaApi.post(`${WAHA_API_URL}/api/sendText`, payload, { timeout: 10000 });
                return response.data;
            } catch (retryErr) {
                console.error(`[WAHA Service] Retry sendWhatsAppMessage failed:`, retryErr.message);
                throw retryErr;
            }
        }
        throw err;
    }
}

// Wrapper for existing sendTenantMessage usage (supports optional mediaUrl)
async function sendTenantMessage(tenantId, phone, message, mediaUrl = null) {
    const sessionName = `tenant_${tenantId}`;
    let cleanPhone = phone.replace(/\D/g, '');
    if (cleanPhone.startsWith('0')) {
        cleanPhone = '62' + cleanPhone.slice(1);
    }
    if (!cleanPhone.endsWith('@c.us')) {
        cleanPhone = cleanPhone + '@c.us';
    }

    // Ensure session is started if stopped/failed/non-existent
    try {
        const response = await wahaApi.get(`${WAHA_API_URL}/api/sessions`, { timeout: 3000 });
        const sessionsList = response.data;
        const found = sessionsList.find(s => s.name === sessionName);

        if (!found || found.status === 'STOPPED' || found.status === 'FAILED') {
            console.log(`[WAHA Service] Session ${sessionName} is stopped or not found. Automatically starting...`);
            await startTenantSession(tenantId);
            await new Promise(resolve => setTimeout(resolve, 3000));
        }
    } catch (sessionCheckErr) {
        console.warn(`[WAHA Service] Pre-send session check error for ${sessionName}:`, sessionCheckErr.message);
    }

    try {
        let result;
        if (mediaUrl) {
            const payload = {
                chatId: cleanPhone,
                file: {
                    url: mediaUrl
                },
                caption: message,
                session: sessionName
            };
            const response = await wahaApi.post(`${WAHA_API_URL}/api/sendFile`, payload, { timeout: 15000 });
            result = response.data;
        } else {
            const payload = {
                chatId: cleanPhone,
                text: message,
                session: sessionName
            };
            const response = await wahaApi.post(`${WAHA_API_URL}/api/sendText`, payload, { timeout: 10000 });
            result = response.data;
        }
        return result;
    } catch (err) {
        console.error(`[WAHA Service] sendTenantMessage failed for tenant ${tenantId}:`, err.message);
        
        // If the error says session is not running/closed, try starting session and retrying once!
        const errMsg = err.message || '';
        const responseData = err.response && err.response.data && JSON.stringify(err.response.data);
        const combinedError = (errMsg + ' ' + (responseData || '')).toLowerCase();
        
        if (combinedError.includes('session') && (combinedError.includes('not running') || combinedError.includes('closed') || combinedError.includes('not active') || combinedError.includes('not found') || combinedError.includes('failed'))) {
            try {
                console.log(`[WAHA Service] Retrying sendTenantMessage after auto-starting session ${sessionName}...`);
                await startTenantSession(tenantId);
                await new Promise(resolve => setTimeout(resolve, 5000)); // wait longer for startup
                
                let result;
                if (mediaUrl) {
                    const payload = {
                        chatId: cleanPhone,
                        file: {
                            url: mediaUrl
                        },
                        caption: message,
                        session: sessionName
                    };
                    const response = await wahaApi.post(`${WAHA_API_URL}/api/sendFile`, payload, { timeout: 15000 });
                    result = response.data;
                } else {
                    const payload = {
                        chatId: cleanPhone,
                        text: message,
                        session: sessionName
                    };
                    const response = await wahaApi.post(`${WAHA_API_URL}/api/sendText`, payload, { timeout: 10000 });
                    result = response.data;
                }
                return result;
            } catch (retryErr) {
                console.error(`[WAHA Service] Retry sendTenantMessage failed:`, retryErr.message);
                throw retryErr;
            }
        }
        throw err;
    }
}

// Check session status and botNumber
async function getTenantStatus(tenantId) {
    const sessionName = `tenant_${tenantId}`;
    try {
        const response = await wahaApi.get(`${WAHA_API_URL}/api/sessions`, { timeout: 3000 });
        const sessionsList = response.data;
        const found = sessionsList.find(s => s.name === sessionName);

        if (!found) {
            return { connected: false, status: 'DISCONNECTED', botNumber: null };
        }

        const status = found.status;
        if (status === 'CONNECTED' || status === 'WORKING') {
            let botNumber = null;
            try {
                const meResponse = await wahaApi.get(`${WAHA_API_URL}/api/${sessionName}/contacts/me`, { timeout: 2000 });
                if (meResponse.data && meResponse.data.id) {
                    botNumber = meResponse.data.id.split('@')[0];
                }
            } catch (e) {
                try {
                    const meResponse2 = await wahaApi.get(`${WAHA_API_URL}/api/contacts/me?session=${sessionName}`, { timeout: 2000 });
                    if (meResponse2.data && meResponse2.data.id) {
                        botNumber = meResponse2.data.id.split('@')[0];
                    }
                } catch (e2) {}
            }

            await updateTenantWaStatus(tenantId, 'CONNECTED', botNumber);
            return { connected: true, status: 'CONNECTED', botNumber };
        } else if (status === 'SCAN_QR' || status === 'SCAN_QR_CODE' || status === 'STARTING') {
            await updateTenantWaStatus(tenantId, 'CONNECTING', null);
            return { connected: false, status: 'CONNECTING', botNumber: null };
        } else {
            await updateTenantWaStatus(tenantId, 'DISCONNECTED', null);
            return { connected: false, status: 'DISCONNECTED', botNumber: null };
        }
    } catch (err) {
        console.error(`[WAHA Service] Error in getTenantStatus: ${err.message}`);
        return { connected: false, status: 'DISCONNECTED', botNumber: null };
    }
}

// Disconnect tenant session
async function disconnectTenant(tenantId) {
    const sessionName = `tenant_${tenantId}`;
    try {
        await wahaApi.delete(`${WAHA_API_URL}/api/sessions/${sessionName}`, { timeout: 5000 });
    } catch (err) {
        console.warn(`[WAHA Service] DELETE failed, trying logout: ${err.message}`);
        try {
            await wahaApi.post(`${WAHA_API_URL}/api/sessions/logout`, { name: sessionName }, { timeout: 5000 });
        } catch (logoutErr) {
            try {
                await wahaApi.post(`${WAHA_API_URL}/api/sessions/stop`, { name: sessionName }, { timeout: 5000 });
            } catch (stopErr) {
                console.error(`[WAHA Service] Disconnect error for ${sessionName}:`, stopErr.message);
            }
        }
    }
    await updateTenantWaStatus(tenantId, 'DISCONNECTED', null);
}

// Auto-start active sessions on startup
async function loadAllTenantSessions() {
    if (!masterPool) return;
    try {
        const [rows] = await masterPool.query('SELECT tenant_id FROM tenant_whatsapp_sessions WHERE status = "CONNECTED" OR status = "PAIRING" OR status = "CONNECTING"');
        for (const row of rows) {
            await startTenantSession(row.tenant_id).catch(() => {});
        }
    } catch (err) {
        console.error('[WAHA Service] Failed to load tenant sessions:', err.message);
    }
}

// Router for Express mount
router.get('/status/:tenant_id', async (req, res) => {
    const statusData = await getTenantStatus(req.params.tenant_id);
    res.json(statusData);
});

router.get('/qr/:tenant_id', async (req, res) => {
    const qrData = await getTenantQR(req.params.tenant_id);
    res.json(qrData);
});

// Supports both POST /send and POST /send/:tenant_id
router.post('/send', async (req, res) => {
    const { tenantId, phone, message } = req.body;
    if (!tenantId || !phone || !message) {
        return res.status(400).json({ error: 'tenantId, phone, and message are required' });
    }
    try {
        const result = await sendTenantMessage(tenantId, phone, message);
        await logMessageHistory(tenantId, phone, message, 'TERKIRIM');
        res.json({ status: 'success', info: 'Message sent successfully', result });
    } catch (err) {
        await logMessageHistory(tenantId, phone, message, 'GAGAL');
        res.status(500).json({ error: err.message });
    }
});

router.post('/send/:tenant_id', async (req, res) => {
    const tenantId = req.params.tenant_id;
    const { phone, message } = req.body;
    if (!phone || !message) {
        return res.status(400).json({ error: 'phone and message are required' });
    }
    try {
        const result = await sendTenantMessage(tenantId, phone, message);
        await logMessageHistory(tenantId, phone, message, 'TERKIRIM');
        res.json({ status: 'success', info: 'Message sent successfully', result });
    } catch (err) {
        await logMessageHistory(tenantId, phone, message, 'GAGAL');
        res.status(500).json({ error: err.message });
    }
});

router.post('/disconnect/:tenant_id', async (req, res) => {
    try {
        await disconnectTenant(req.params.tenant_id);
        res.json({ status: 'success', message: 'WhatsApp logged out and session cleared' });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

module.exports = {
    router,
    startTenantSession,
    getTenantQR,
    sendWhatsAppMessage,
    sendTenantMessage,
    setMasterPool,
    setGetTenantPool,
    loadAllTenantSessions,
    sessions: {}
};
