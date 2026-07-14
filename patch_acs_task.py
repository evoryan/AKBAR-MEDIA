import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """app.post('/api/acs/devices/:id/action', async (req, res) => {
    try {
        const { action, value } = req.body;
        // Mock successful response since we can't easily proxy tasks without knowing the exact ACS task API format.
        // In a real app we'd POST to /tasks for the specific device ID.
        res.json({ message: `Aksi ${action} diproses` });
    } catch (error) {
        console.error("API Error:", error.message); res.status(500).json({ error: (error && error.message) ? error.message : "Terjadi kesalahan" });
    }
});"""

rep = """app.post('/api/acs/devices/:id/action', async (req, res) => {
    try {
        const { action, value, areaName } = req.body;
        const deviceId = req.params.id;
        
        let areaRow = null;
        if (areaName) {
            const [areas] = await req.pool.query('SELECT * FROM areas WHERE name = ?', [areaName]);
            if (areas.length > 0) areaRow = areas[0];
        }
        
        if (!areaRow) {
            // Fallback to searching all areas
            const [areas] = await req.pool.query('SELECT * FROM areas WHERE apiDomain IS NOT NULL AND apiDomain != ""');
            areaRow = areas[0]; // just try the first one for now if we can't find it
        }
        
        if (!areaRow || !areaRow.apiDomain) {
            return res.status(400).json({ error: "Server Area tidak ditemukan atau URL API kosong" });
        }
        
        let baseUrl = areaRow.apiDomain.trim();
        if (baseUrl.endsWith('/')) baseUrl = baseUrl.slice(0, -1);
        
        let taskData = {
            device: deviceId
        };
        
        if (action === 'set_ssid') {
            taskData.name = 'setParameterValues';
            taskData.parameterValues = [
                ['InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSID', String(value), 'xsd:string'],
                ['Device.WiFi.SSID.1.SSID', String(value), 'xsd:string']
            ];
        } else if (action === 'set_password') {
            taskData.name = 'setParameterValues';
            taskData.parameterValues = [
                ['InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.PreSharedKey', String(value), 'xsd:string'],
                ['InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.KeyPassphrase', String(value), 'xsd:string'],
                ['Device.WiFi.Radio.1.Security.KeyPassphrase', String(value), 'xsd:string']
            ];
        } else if (action === 'reboot') {
            taskData.name = 'reboot';
        } else {
            return res.status(400).json({ error: "Aksi tidak dikenal" });
        }
        
        // GenieACS v1.2 format: POST /tasks?connection_request
        const response = await axios.post(`${baseUrl}/tasks?connection_request`, taskData, {
            auth: { username: areaRow.acsUser, password: areaRow.acsPassword },
            timeout: 10000 // give it some time to process
        });
        
        res.json({ message: `Aksi ${action} berhasil diproses` });
    } catch (error) {
        console.error("ACS Task Error:", error.message); 
        let errorMsg = error.message;
        if (error.response && error.response.data) {
            errorMsg = typeof error.response.data === 'string' ? error.response.data : JSON.stringify(error.response.data);
        }
        res.status(500).json({ error: `Gagal ke ACS: ${errorMsg}` });
    }
});"""

if target in content:
    content = content.replace(target, rep)
    with open("VPS/server.js", "w") as f:
        f.write(content)
    print("Patched server.js for ACS action")
else:
    print("Target not found for ACS action")

