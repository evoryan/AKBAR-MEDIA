import re

with open('app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt', 'r') as f:
    content = f.read()

imports = """import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import com.example.ui.data.remote.ApiClient
import com.example.ui.screens.Customer
import android.widget.Toast
"""

if "import android.net.Uri" not in content:
    content = content.replace("import androidx.compose.runtime.Composable\n", "import androidx.compose.runtime.Composable\n" + imports)


# Find the start of PaymentSuccessScreen
old_start = """fun PaymentSuccessScreen(customerId: String, totalAmount: String, months: String, onFinish: () -> Unit) {
    val bgMain = Color(0xFF05050A)"""
    
new_start = """fun PaymentSuccessScreen(customerId: String, totalAmount: String, months: String, onFinish: () -> Unit) {
    val context = LocalContext.current
    var customer by remember { mutableStateOf<Customer?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(customerId) {
        try {
            val custs = ApiClient.apiService.getCustomers()
            customer = custs.find { it.id == customerId }
        } catch(e: Exception) {
            Toast.makeText(context, "Gagal memuat data pelanggan", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    val bgMain = Color(0xFF05050A)"""

content = content.replace(old_start, new_start)

# Replace the hardcoded ReceiptRows
old_rows = """            ReceiptRow("Alamat", "Pakisan", textMain)
            DividerLine()
            ReceiptRow("Area", "Pakisan", textMain)
            DividerLine()
            ReceiptRow("Iuran", formattedAmount, textMain, isBold = true)
            DividerLine()
            ReceiptRow("Biaya Tambahan", "- Rp. 0\n- Rp. 0", textMain)
            DividerLine()
            ReceiptRow("PPN 0%", "Rp. 0", textMain)
            DividerLine()
            ReceiptRow("Diskon", "Rp. 0", textMain)"""

new_rows = """            ReceiptRow("Nama", customer?.name ?: "-", textMain)
            DividerLine()
            ReceiptRow("Area", customer?.area ?: "-", textMain)
            DividerLine()
            ReceiptRow("Iuran", formattedAmount, textMain, isBold = true)
            DividerLine()
            val add1 = customer?.additionalCost1?.takeIf { it.isNotBlank() } ?: "Rp. 0"
            val add2 = customer?.additionalCost2?.takeIf { it.isNotBlank() } ?: "Rp. 0"
            ReceiptRow("Biaya Tambahan", "- $add1\n- $add2", textMain)
            DividerLine()
            ReceiptRow("Diskon", customer?.discount?.takeIf { it.isNotBlank() }?.let { "- $it" } ?: "Rp. 0", textMain)"""
            
content = content.replace(old_rows, new_rows)

# Update ActionIconBtn
old_action_btn = """@Composable
fun ActionIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, bg: Color, contentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp, horizontal = 24.dp)
    ) {"""
new_action_btn = """@Composable
fun ActionIconBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, bg: Color, contentColor: Color, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 24.dp)
    ) {"""
content = content.replace(old_action_btn, new_action_btn)

# Update Kirim WA action
old_actions = """        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionIconBtn(Icons.Default.Print, "Cetak", cardBg, neonCyan)
            ActionIconBtn(Icons.Default.Message, "Kirim WA", cardBg, neonCyan)
            ActionIconBtn(Icons.Default.Share, "Bagikan", cardBg, neonCyan)
        }"""
        
new_actions = """        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionIconBtn(Icons.Default.Print, "Cetak", cardBg, neonCyan) {
                Toast.makeText(context, "Fitur cetak belum tersedia", Toast.LENGTH_SHORT).show()
            }
            ActionIconBtn(Icons.AutoMirrored.Filled.Message, "Kirim WA", cardBg, neonCyan) {
                val phone = customer?.phone
                if (!phone.isNullOrBlank()) {
                    try {
                        var formattedPhone = phone
                        if (formattedPhone.startsWith("0")) {
                            formattedPhone = "62" + formattedPhone.substring(1)
                        }
                        val text = "Halo ${customer?.name},\\nTerima kasih, pembayaran tagihan internet untuk bulan ${months} sejumlah ${formattedAmount} telah kami terima dan lunas.\\n\\nSalam,\\n${com.example.ui.data.SettingsManager.companyName}"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(text)}")
                        context.startActivity(intent)
                    } catch(e: Exception) {
                        Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Nomor pelanggan tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
            ActionIconBtn(Icons.Default.Share, "Bagikan", cardBg, neonCyan) {
                val text = "Bukti Pembayaran Tagihan Internet\\nNama: ${customer?.name}\\nTotal: $formattedAmount\\nBulan: $months\\nStatus: LUNAS"
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            }
        }"""
content = content.replace(old_actions, new_actions)

with open('app/src/main/java/com/example/ui/screens/PaymentSuccessScreen.kt', 'w') as f:
    f.write(content)
