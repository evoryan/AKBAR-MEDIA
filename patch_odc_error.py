import re

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.unit.dp", "import androidx.compose.ui.unit.dp\nimport android.widget.Toast\nimport androidx.compose.ui.platform.LocalContext\nimport android.util.Log")
content = content.replace("fun OdcScreen(onBack: () -> Unit) {", "fun OdcScreen(onBack: () -> Unit) {\n    val context = LocalContext.current")

old_catch = "                            } catch(e: Exception) {}"
new_catch = """                            } catch(e: Exception) {
                                Log.e("OdcScreen", "Error adding ODC", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }"""
content = content.replace(old_catch, new_catch)

old_catch2 = "                            } catch (e: Exception) {}"
new_catch2 = """                            } catch (e: Exception) {
                                Log.e("OdcScreen", "Error updating ODC", e)
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }"""
content = content.replace(old_catch2, new_catch2)

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(content)
