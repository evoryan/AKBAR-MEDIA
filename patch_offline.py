import re

with open("app/src/main/java/com/example/ui/data/remote/OfflineInterceptor.kt", "r") as f:
    content = f.read()

target_merge = """            // 3. Process Pending PUTs (Updates)"""

rep_merge = """            // 2.5 Process Pending Billing Pay/Delete
            val pendingPays = syncDao.getPendingForUrl("/api/billing/pay").filter { it.method == "POST" }
            for (pay in pendingPays) {
                try {
                    val payObj = JSONObject(pay.body)
                    val customerId = payObj.getString("customerId")
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        if (obj.has("id") && obj.getString("id") == customerId) {
                            obj.put("status", "LUNAS CASH")
                            array.put(i, obj)
                            break
                        }
                    }
                } catch(e: Exception) {}
            }
            val pendingBillingDeletes = syncDao.getPendingForUrl("/api/billing/delete").filter { it.method == "POST" }
            for (del in pendingBillingDeletes) {
                try {
                    val delObj = JSONObject(del.body)
                    val customerId = delObj.getString("customerId")
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        if (obj.has("id") && obj.getString("id") == customerId) {
                            obj.put("status", "BELUM BAYAR")
                            array.put(i, obj)
                            break
                        }
                    }
                } catch(e: Exception) {}
            }

            // 3. Process Pending PUTs (Updates)"""

content = content.replace(target_merge, rep_merge)

with open("app/src/main/java/com/example/ui/data/remote/OfflineInterceptor.kt", "w") as f:
    f.write(content)
