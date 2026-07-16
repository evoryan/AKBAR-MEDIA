import re

with open("VPS/server.js", "r") as f:
    content = f.read()

# Replace all await client.rosApi.write commands that can throw !empty with try-catch
# For disable
disable_pattern = """            await client.rosApi.write('/ppp/secret/disable', [
                `=.id=${realId}`
            ]);"""
disable_replacement = """            try {
                await client.rosApi.write('/ppp/secret/disable', [
                    `=.id=${realId}`
                ]);
            } catch (err) {
                if (err.message && err.message.includes('!empty')) {
                    console.log("Ignored !empty response from Mikrotik (Disable)");
                } else {
                    throw err;
                }
            }"""
content = content.replace(disable_pattern, disable_replacement)

# For enable
enable_pattern = """        await client.rosApi.write('/ppp/secret/enable', [
            `=.id=${realId}`
        ]);"""
enable_replacement = """        try {
            await client.rosApi.write('/ppp/secret/enable', [
                `=.id=${realId}`
            ]);
        } catch (err) {
            if (err.message && err.message.includes('!empty')) {
                console.log("Ignored !empty response from Mikrotik (Enable)");
            } else {
                throw err;
            }
        }"""
content = content.replace(enable_pattern, enable_replacement)

# For remove active
remove_pattern = """                        await client.rosApi.write('/ppp/active/remove', [
                            `=.id=${activeId}`
                        ]);"""
remove_replacement = """                        try {
                            await client.rosApi.write('/ppp/active/remove', [
                                `=.id=${activeId}`
                            ]);
                        } catch (err) {
                            if (err.message && err.message.includes('!empty')) {
                                console.log("Ignored !empty response from Mikrotik (Active Remove)");
                            } else {
                                throw err;
                            }
                        }"""
content = content.replace(remove_pattern, remove_replacement)

# Another remove active (might be indented differently)
remove_pattern2 = """                    await client.rosApi.write('/ppp/active/remove', [
                        `=.id=${activeId}`
                    ]);"""
remove_replacement2 = """                    try {
                        await client.rosApi.write('/ppp/active/remove', [
                            `=.id=${activeId}`
                        ]);
                    } catch (err) {
                        if (err.message && err.message.includes('!empty')) {
                            console.log("Ignored !empty response from Mikrotik (Active Remove)");
                        } else {
                            throw err;
                        }
                    }"""
content = content.replace(remove_pattern2, remove_replacement2)

# Third remove active 
remove_pattern3 = """            await client.rosApi.write('/ppp/active/remove', [
                `=.id=${activeId}`
            ]);"""
remove_replacement3 = """            try {
                await client.rosApi.write('/ppp/active/remove', [
                    `=.id=${activeId}`
                ]);
            } catch (err) {
                if (err.message && err.message.includes('!empty')) {
                    console.log("Ignored !empty response from Mikrotik (Active Remove)");
                } else {
                    throw err;
                }
            }"""
content = content.replace(remove_pattern3, remove_replacement3)

# Another disable
disable_pattern2 = """        await client.rosApi.write('/ppp/secret/disable', [
            `=.id=${realId}`
        ]);"""
disable_replacement2 = """        try {
            await client.rosApi.write('/ppp/secret/disable', [
                `=.id=${realId}`
            ]);
        } catch (err) {
            if (err.message && err.message.includes('!empty')) {
                console.log("Ignored !empty response from Mikrotik (Disable)");
            } else {
                throw err;
            }
        }"""
content = content.replace(disable_pattern2, disable_replacement2)


with open("VPS/server.js", "w") as f:
    f.write(content)

