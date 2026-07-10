import re

with open('VPS/server.js', 'r') as f:
    content = f.read()

old_block = """                if (response.data && Array.isArray(response.data)) {
                    const devices = response.data.map(d => {
                        const summary = d.summary || {};
                        const pppoeUser = d.InternetGatewayDevice?.WANDevice?.['1']?.WANPPPConnection?.['1']?.Username?._value || 
                                          d.Device?.WANDevice?.['1']?.WANPPPConnection?.['1']?.Username?._value || 
                                          'Unknown';
                        
                        const ssid = d.InternetGatewayDevice?.LANDevice?.['1']?.WLANConfiguration?.['1']?.SSID?._value || 
                                     d.Device?.WiFi?.SSID?.['1']?.SSID?._value || 
                                     'Unknown';

                        return {
                            id: d._id || Math.random().toString(36).substring(7),
                            username: summary.username || pppoeUser || d._id,
                            isOnline: (d._lastPing && (new Date() - new Date(d._lastPing)) < 300000) ? true : false,
                            ssid: ssid,
                            connectedUsers: 0,
                            customerNumber: '-',
                            rxPower: '-',
                            areaName: area.name
                        };
                    });
                    allDevices = allDevices.concat(devices);
                }"""

new_block = """                if (response.data && Array.isArray(response.data)) {
                    const devices = response.data.map(d => {
                        const getVal = (path) => {
                            if (d[path] && d[path]._value !== undefined) return d[path]._value;
                            if (d[path] !== undefined && typeof d[path] !== 'object') return d[path];
                            
                            const parts = path.split('.');
                            let current = d;
                            for (const part of parts) {
                                if (current == null) return undefined;
                                current = current[part];
                            }
                            if (current && current._value !== undefined) return current._value;
                            return current;
                        };

                        const pppoeUser = 
                            getVal('InternetGatewayDevice.WANDevice.1.WANConnectionDevice.1.WANPPPConnection.1.Username') ||
                            getVal('InternetGatewayDevice.WANDevice.1.WANPPPConnection.1.Username') ||
                            getVal('Device.WANDevice.1.WANPPPConnection.1.Username') ||
                            getVal('Device.Users.User.1.Username') ||
                            (d.summary && d.summary.username) ||
                            (d.summary && d.summary.mac) ||
                            'Unknown';
                            
                        const ssid = 
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSID') ||
                            getVal('Device.WiFi.SSID.1.SSID') ||
                            'Unknown';
                            
                        const rxPowerRaw = 
                            getVal('InternetGatewayDevice.WANDevice.1.X_ZTE-COM_WANPONInterfaceConfig.RXPower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.X_ZTE_COM_WANPONInterfaceConfig.RXPower') ||
                            getVal('Device.Optical.ReceivePower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.WANDSLInterfaceConfig.RxPower') ||
                            '-';
                            
                        let rxPowerStr = '-';
                        if (rxPowerRaw !== undefined && rxPowerRaw !== null && rxPowerRaw !== '-' && rxPowerRaw !== '') {
                            let num = parseFloat(rxPowerRaw);
                            if (!isNaN(num)) {
                                if (Math.abs(num) > 100) num = num / 1000;
                                else if (Math.abs(num) > 10 && num > 0) num = num / 100;
                                rxPowerStr = num.toFixed(2);
                            } else {
                                rxPowerStr = String(rxPowerRaw);
                            }
                        }
                        
                        const connectedUsers = 
                            getVal('InternetGatewayDevice.LANDevice.1.Hosts.HostNumberOfEntries') ||
                            getVal('Device.Hosts.HostNumberOfEntries') ||
                            (d.summary && d.summary.connectedUsers) ||
                            '0';

                        const lastInform = d._lastInform || d._lastPing || d._lastBoot;
                        let isOnline = false;
                        if (lastInform) {
                            const diffMs = new Date() - new Date(lastInform);
                            isOnline = diffMs < 20 * 60 * 1000; // 20 mins threshold
                        }

                        return {
                            id: d._id || Math.random().toString(36).substring(7),
                            username: pppoeUser !== 'Unknown' ? String(pppoeUser) : (d._id || 'Unknown'),
                            isOnline: isOnline,
                            ssid: String(ssid),
                            connectedUsers: parseInt(connectedUsers) || 0,
                            customerNumber: '-',
                            rxPower: rxPowerStr,
                            areaName: area.name
                        };
                    });
                    allDevices = allDevices.concat(devices);
                }"""

if old_block in content:
    content = content.replace(old_block, new_block)
    with open('VPS/server.js', 'w') as f:
        f.write(content)
    print("Patched successfully")
else:
    print("Old block not found!")
