import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """                        const rxPowerRaw = (() => {
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
                                if (num !== 0) {
                                    if (num > 32767) num -= 65536; // 16-bit unsigned to signed
                                    
                                    if (num > 0) {
                                        // If it's a large positive value, it's likely 0.1 uW (microwatts) or similar
                                        if (num > 100) {
                                            // conversion from 0.1uW to dBm: 10 * log10(val / 10000)
                                            // Some Huawei use this.
                                            let dbm = 10 * Math.log10(num / 10000);
                                            // Sanity check: valid GPON Rx is typically -8 to -35
                                            if (dbm < 0 && dbm > -40) {
                                                num = dbm;
                                            } else {
                                                // Maybe it was just positive dBm * 100?
                                                num = -(num / 100);
                                            }
                                        } else {
                                            // Small positive value, assume they omitted the negative sign
                                            num = -num;
                                        }
                                    } else {
                                        // Negative values
                                        if (num < -10000) num /= 1000; // e.g. -23000 -> -23.00
                                        else if (num < -1000) num /= 100; // e.g. -2350 -> -23.50
                                        else if (num < -100) num /= 10; // e.g. -235 -> -23.5
                                    }
                                }
                                rxPowerStr = num.toFixed(2);
                            } else {
                                rxPowerStr = String(rxPowerRaw);
                            }
                        }"""

rep = """                        let rxPowerStr = '-';
                        const getAny = (pathTpl) => {
                            if (pathTpl.includes('*')) {
                                for (let i = 1; i <= 5; i++) {
                                    let v = getVal(pathTpl.replace('*', i));
                                    if (v !== undefined && v !== null && v !== '') return v;
                                }
                            } else {
                                let v = getVal(pathTpl);
                                if (v !== undefined && v !== null && v !== '') return v;
                            }
                            return undefined;
                        };

                        let zte = getAny("InternetGatewayDevice.WANDevice.*.X_ZTE-COM_WANPONInterfaceConfig.RXPower") || getAny("InternetGatewayDevice.WANDevice.*.X_ZTE_COM_WANPONInterfaceConfig.RXPower");
                        let huawei = getAny("InternetGatewayDevice.WANDevice.*.X_GponInterafceConfig.RXPower") || getAny("InternetGatewayDevice.WANDevice.*.X_HW_Optical.RxPower") || getAny("InternetGatewayDevice.WANDevice.*.X_HW_Optical.RxOpticalPower");
                        let fiberhome = getAny("InternetGatewayDevice.WANDevice.*.X_FH_GponInterfaceConfig.RXPower");
                        let ztecmcc = getAny("InternetGatewayDevice.WANDevice.*.X_CMCC_EponInterfaceConfig.RXPower");
                        let ztecmcg = getAny("InternetGatewayDevice.WANDevice.*.X_CMCC_GponInterfaceConfig.RXPower");
                        let gm220se = getAny("InternetGatewayDevice.WANDevice.*.X_CT-COM_EponInterfaceConfig.RXPower");
                        let gm220sg = getAny("InternetGatewayDevice.WANDevice.*.X_CT-COM_GponInterfaceConfig.RXPower") || getAny("InternetGatewayDevice.WANDevice.*.X_CT-COM_PONInterfaceConfig.RXPower") || getAny("InternetGatewayDevice.WANDevice.*.X_CT-COM_XPON.RXPower");
                        let f477v2 = getAny("InternetGatewayDevice.WANDevice.*.X_CU_WANEPONInterfaceConfig.OpticalTransceiver.RXPower");
                        let nokia = getAny("InternetGatewayDevice.X_ALU_OntOpticalParam.RXPower");
                        let generic = getAny("Device.Optical.ReceivePower") || getAny("InternetGatewayDevice.WANDevice.*.WANDSLInterfaceConfig.RxPower");
                        
                        const calcDb = (val, isLog) => {
                            let num = parseFloat(val);
                            if (isNaN(num)) return val;
                            if (num < 0) {
                                if (num < -1000) num = num / 1000;
                                else if (num < -100) num = num / 100;
                                return num.toFixed(2);
                            } else if (num > 0 && isLog) {
                                let db = 30 + (Math.log10(num * Math.pow(10, -7)) * 10);
                                return (Math.ceil(db * 100) / 100).toFixed(2);
                            } else if (num > 0 && !isLog) {
                                if (num > 1000) num = -(num / 100);
                                else if (num > 100) num = -(num / 10);
                                else num = -num;
                                return num.toFixed(2);
                            }
                            return num.toFixed(2);
                        };

                        let m = undefined;
                        if (zte !== undefined) m = calcDb(zte, true);
                        else if (ztecmcc !== undefined) m = calcDb(ztecmcc, true);
                        else if (ztecmcg !== undefined) m = calcDb(ztecmcg, true);
                        else if (gm220se !== undefined) m = calcDb(gm220se, true);
                        else if (gm220sg !== undefined) m = calcDb(gm220sg, true);
                        else if (f477v2 !== undefined) m = calcDb(f477v2, true);
                        else if (huawei !== undefined) m = calcDb(huawei, false);
                        else if (fiberhome !== undefined) m = calcDb(fiberhome, false);
                        else if (nokia !== undefined) m = calcDb(nokia, false);
                        else if (generic !== undefined) m = calcDb(generic, false);

                        if (m !== undefined && m !== "N/A" && m !== null) {
                            rxPowerStr = String(m);
                        }"""

if target in content:
    content = content.replace(target, rep)
    with open("VPS/server.js", "w") as f:
        f.write(content)
    print("Patched server.js for precise rxPowerStr from user script")
else:
    print("Target not found for rxPowerStr")

