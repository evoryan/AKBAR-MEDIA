import re

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('} catch(e: retrofit2.HttpException) {\\n                                        val errBody = e.response()?.errorBody()?.string()\\n                                        android.util.Log.e("AddCust", "HTTP Error: $errBody", e)\\n                                        android.widget.Toast.makeText(context, "Error: $errBody", android.widget.Toast.LENGTH_LONG).show()\\n                                    } catch (e: Exception) {\\n                                        android.util.Log.e("AddCust", "Exception", e)\\n                                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()\\n                                    }', '''} catch(e: retrofit2.HttpException) {
                                        val errBody = e.response()?.errorBody()?.string()
                                        android.util.Log.e("AddCust", "HTTP Error: $errBody", e)
                                        android.widget.Toast.makeText(context, "Error: $errBody", android.widget.Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        android.util.Log.e("AddCust", "Exception", e)
                                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }''')

with open('app/src/main/java/com/example/ui/screens/AddCustomerScreen.kt', 'w') as f:
    f.write(content)
