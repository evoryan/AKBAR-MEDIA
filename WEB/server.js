const express = require('express');
const path = require('path');
const http = require('http');
const https = require('https');
const app = express();

// Determine port from environment or fallback
const PORT = process.env.PORT || 8080;
const BACKEND_URL = process.env.BACKEND_URL || process.env.API_URL || 'http://103.253.245.25:4100';

// Built-in reverse proxy for /api and /uploads to VPS Backend Server
app.use(['/api', '/uploads'], (req, res) => {
    try {
        const target = new URL(req.originalUrl, BACKEND_URL);
        const isHttps = target.protocol === 'https:';
        const client = isHttps ? https : http;

        const headers = { ...req.headers };
        headers.host = target.host;
        
        const clientIp = req.headers['x-forwarded-for'] || req.socket.remoteAddress;
        if (clientIp) {
            headers['x-forwarded-for'] = clientIp;
        }

        const options = {
            protocol: target.protocol,
            hostname: target.hostname,
            port: target.port || (isHttps ? 443 : 80),
            method: req.method,
            path: target.pathname + target.search,
            headers: headers
        };

        const proxyReq = client.request(options, (proxyRes) => {
            res.writeHead(proxyRes.statusCode, proxyRes.headers);
            proxyRes.pipe(res);
        });

        proxyReq.on('error', (err) => {
            console.error(`[Web Proxy Error] ${req.method} ${req.originalUrl} ->`, err.message);
            if (!res.headersSent) {
                res.status(502).json({
                    error: `Gagal terhubung ke backend server (${BACKEND_URL}): ${err.message}`
                });
            }
        });

        req.pipe(proxyReq);
    } catch (err) {
        console.error('[Web Proxy Exception]', err.message);
        if (!res.headersSent) {
            res.status(500).json({ error: 'Proxy internal error: ' + err.message });
        }
    }
});

// Serve static assets from public folder
app.use(express.static(path.join(__dirname, 'public')));

// Fallback to index.html for SPA routing
app.get('*', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
    console.log('===================================================');
    console.log(`🚀 Akbar Media Web Frontend is running!`);
    console.log(`🌐 URL: http://localhost:${PORT}`);
    console.log(`🔗 Backend Proxy Target: ${BACKEND_URL}`);
    console.log(`📂 Serving static files from: ${path.join(__dirname, 'public')}`);
    console.log('===================================================');
});
