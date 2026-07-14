import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """                        const pppoeUser = 
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
                            '-';"""

rep = """                        const pppoeUser = (() => {
                            for (let i = 1; i <= 5; i++) {
                                for (let j = 1; j <= 3; j++) {
                                    let u = getVal(`InternetGatewayDevice.WANDevice.1.WANConnectionDevice.${i}.WANPPPConnection.${j}.Username`);
                                    if (u) return u;
                                }
                            }
                            return getVal('InternetGatewayDevice.WANDevice.1.WANPPPConnection.1.Username') ||
                                   getVal('Device.WANDevice.1.WANPPPConnection.1.Username') ||
                                   getVal('Device.Users.User.1.Username') ||
                                   (d.summary && d.summary.username) ||
                                   (d.summary && d.summary.mac) ||
                                   'Unknown';
                        })();
                            
                        const ssid = 
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSID') ||
                            getVal('Device.WiFi.SSID.1.SSID') ||
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.SSIDAdvertisementEnabled') ||
                            'Unknown';
                            
                        const wifiPassword = 
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.PreSharedKey') ||
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.KeyPassphrase') ||
                            getVal('Device.WiFi.Radio.1.Security.KeyPassphrase') ||
                            '-';
                            
                        const rxPowerRaw = 
                            getVal('InternetGatewayDevice.WANDevice.1.X_ZTE-COM_WANPONInterfaceConfig.RXPower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.X_ZTE_COM_WANPONInterfaceConfig.RXPower') ||
                            getVal('Device.Optical.ReceivePower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.WANDSLInterfaceConfig.RxPower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.X_CT-COM_PONInterfaceConfig.RXPower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.X_HW_Optical.RxPower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.X_HW_Optical.RxOpticalPower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.X_CT-COM_XPON.RXPower') ||
                            getVal('InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower') ||
                            '-';"""

if target in content:
    content = content.replace(target, rep)
else:
    print("TARGET NOT FOUND 1")

target2 = """                            id: d._id || Math.random().toString(36).substring(7),
                            username: pppoeUser !== 'Unknown' ? String(pppoeUser) : (d._id || 'Unknown'),
                            isOnline: isOnline,
                            ssid: String(ssid),
                            connectedUsers: parseInt(connectedUsers) || 0,
                            customerNumber: '-',
                            rxPower: rxPowerStr,
                            areaName: area.name
                        };"""

rep2 = """                            id: d._id || Math.random().toString(36).substring(7),
                            username: pppoeUser !== 'Unknown' ? String(pppoeUser) : (d._id || 'Unknown'),
                            isOnline: isOnline,
                            ssid: String(ssid),
                            wifiPassword: String(wifiPassword),
                            connectedUsers: parseInt(connectedUsers) || 0,
                            customerNumber: '-',
                            rxPower: rxPowerStr,
                            areaName: area.name
                        };"""
                        
if target2 in content:
    content = content.replace(target2, rep2)
else:
    print("TARGET NOT FOUND 2")

with open("VPS/server.js", "w") as f:
    f.write(content)

