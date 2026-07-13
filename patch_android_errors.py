import re

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('catch(e: Exception) {', 'catch(e: retrofit2.HttpException) {\n                                val errBody = e.response()?.errorBody()?.string()\n                                Log.e("OdcScreen", "HTTP Error: $errBody", e)\n                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()\n                            } catch(e: Exception) {')

content = content.replace('catch (e: Exception) {', 'catch(e: retrofit2.HttpException) {\n                                val errBody = e.response()?.errorBody()?.string()\n                                Log.e("OdcScreen", "HTTP Error: $errBody", e)\n                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()\n                            } catch (e: Exception) {')

with open('app/src/main/java/com/example/ui/screens/OdcScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('catch(e: Exception) {', 'catch(e: retrofit2.HttpException) {\n                                val errBody = e.response()?.errorBody()?.string()\n                                Log.e("OdpScreen", "HTTP Error: $errBody", e)\n                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()\n                            } catch(e: Exception) {')

content = content.replace('catch (e: Exception) {', 'catch(e: retrofit2.HttpException) {\n                                val errBody = e.response()?.errorBody()?.string()\n                                Log.e("OdpScreen", "HTTP Error: $errBody", e)\n                                Toast.makeText(context, "Server Error: $errBody", Toast.LENGTH_LONG).show()\n                            } catch (e: Exception) {')

with open('app/src/main/java/com/example/ui/screens/OdpScreen.kt', 'w') as f:
    f.write(content)
