import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """                        const wifiPassword = 
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
                            '0';"""

rep = """                        const wifiPassword = 
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.KeyPassphrase') ||
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.PreSharedKey.1.PreSharedKey') ||
                            getVal('InternetGatewayDevice.LANDevice.1.WLANConfiguration.1.KeyPassphrase') ||
                            getVal('Device.WiFi.Radio.1.Security.KeyPassphrase') ||
                            '-';
                            
                        const rxPowerRaw = (() => {
                            for (let i = 1; i <= 5; i++) {
                                let val = getVal(`InternetGatewayDevice.WANDevice.${i}.X_CU_WANEPONInterfaceConfig.OpticalTransceiver.RXPower`);
                                if (val) return val;
                            }
                            return getVal('InternetGatewayDevice.WANDevice.1.X_ZTE-COM_WANPONInterfaceConfig.RXPower') ||
                                getVal('InternetGatewayDevice.WANDevice.1.X_ZTE_COM_WANPONInterfaceConfig.RXPower') ||
                                getVal('Device.Optical.ReceivePower') ||
                                getVal('InternetGatewayDevice.WANDevice.1.WANDSLInterfaceConfig.RxPower') ||
                                getVal('InternetGatewayDevice.WANDevice.1.X_CT-COM_PONInterfaceConfig.RXPower') ||
                                getVal('InternetGatewayDevice.WANDevice.1.X_HW_Optical.RxPower') ||
                                getVal('InternetGatewayDevice.WANDevice.1.X_HW_Optical.RxOpticalPower') ||
                                getVal('InternetGatewayDevice.WANDevice.1.X_CT-COM_XPON.RXPower') ||
                                getVal('InternetGatewayDevice.WANDevice.1.X_CT-COM_GponInterfaceConfig.RXPower') ||
                                '-';
                        })();
                            
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
                        
                        const connectedUsers = (() => {
                            let count = parseInt(getVal('InternetGatewayDevice.LANDevice.1.Hosts.HostNumberOfEntries') || getVal('Device.Hosts.HostNumberOfEntries') || (d.summary && d.summary.connectedUsers) || '0');
                            if (count === 0 || isNaN(count)) {
                                // Try AssociatedDevice
                                let assocCount = 0;
                                for (let i = 1; i <= 3; i++) {
                                    for (let j = 1; j <= 5; j++) {
                                        let assoc = getVal(`InternetGatewayDevice.LANDevice.${i}.WLANConfiguration.${j}.AssociatedDevice`);
                                        if (assoc && typeof assoc === 'object') {
                                            assocCount += Object.keys(assoc).filter(k => !k.startsWith('_')).length;
                                        }
                                    }
                                }
                                if (assocCount > 0) return assocCount;
                            }
                            return count;
                        })();"""

if target in content:
    content = content.replace(target, rep)
    with open("VPS/server.js", "w") as f:
        f.write(content)
    print("Patched server.js")
else:
    print("Target not found in server.js")

