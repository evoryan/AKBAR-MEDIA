import re

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "r") as f:
    content = f.read()

pattern = r"""                                    try \{
                                        val api = GithubApiService\.create\(\)
                                        // TODO: Ganti owner dan repo dengan yang sesuai
                                        val release = api\.getLatestRelease\("satriaevo77", "Akbar-Media"\) // placeholder, will use appropriate later
                                        updateInfo = release
                                        showUpdateDialog = true
                                    \} catch \(e: Exception\) \{
                                        Toast\.makeText\(context, "Gagal memeriksa update: \$\{e\.message\}", Toast\.LENGTH_SHORT\)\.show\(\)
                                    \} finally \{"""

replacement = """                                    try {
                                        val api = GithubApiService.create()
                                        val release = api.getLatestRelease("satriaevo77", "Akbar-Media")
                                        updateInfo = release
                                        showUpdateDialog = true
                                    } catch (e: retrofit2.HttpException) {
                                        if (e.code() == 404) {
                                            Toast.makeText(context, "Belum ada update (404 Not Found)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Gagal memeriksa update: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Gagal memeriksa update: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {"""

content = re.sub(pattern, replacement, content)

with open("app/src/main/java/com/example/ui/screens/SettingScreen.kt", "w") as f:
    f.write(content)

