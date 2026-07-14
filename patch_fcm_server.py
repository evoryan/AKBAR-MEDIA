import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Add require
if "const { sendTenantNotification } =" not in content:
    content = content.replace("const jwt = require('jsonwebtoken');", "const jwt = require('jsonwebtoken');\nconst { sendTenantNotification } = require('./fcm_service');")

# Add to pay endpoint
target_notif = """        // Add notification
        const notifMsg = `Pembayaran "${customerName}" di terima oleh "${adminName}"`;
        try {
            await req.pool.query('INSERT INTO notifications (message) VALUES (?)', [notifMsg]);
        } catch (e) {"""

rep_notif = """        // Add notification
        const notifMsg = `Pembayaran "${customerName}" di terima oleh "${adminName}"`;
        try {
            await req.pool.query('INSERT INTO notifications (message) VALUES (?)', [notifMsg]);
            
            if (req.user && req.user.db_name) {
                sendTenantNotification(req.user.db_name, "Pembayaran Diterima", notifMsg);
            }
        } catch (e) {"""

content = content.replace(target_notif, rep_notif)

with open("VPS/server.js", "w") as f:
    f.write(content)
