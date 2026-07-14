import re

with open("VPS/server.js", "r") as f:
    content = f.read()

target = """                        let rxPowerStr = '-';
                        if (rxPowerRaw !== undefined && rxPowerRaw !== null && rxPowerRaw !== '-' && rxPowerRaw !== '') {
                            let num = parseFloat(rxPowerRaw);
                            if (!isNaN(num)) {
                                if (num !== 0) {
                                    if (num > 32767) num -= 65536; // 16-bit unsigned to signed
                                    
                                    if (num < -10000) num /= 1000;
                                    else if (num < -1000) num /= 100;
                                    else if (num < -100) num /= 10;
                                    else if (num > 10000) num /= 1000;
                                    else if (num > 1000) num /= 100;
                                    else if (num > 100) num /= 10;
                                }
                                rxPowerStr = num.toFixed(2);
                            } else {
                                rxPowerStr = String(rxPowerRaw);
                            }
                        }"""

rep = """                        let rxPowerStr = '-';
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

if target in content:
    content = content.replace(target, rep)
    with open("VPS/server.js", "w") as f:
        f.write(content)
    print("Patched server.js for advanced rxPowerStr")
else:
    print("Target not found for rxPowerStr")

