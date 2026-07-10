package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.UserSession
import com.example.ui.data.UserRole
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BotWaScreen(onBack: () -> Unit) {
    val currentUser by UserSession.currentUser.collectAsState()
    val bgMain = Color(0xFF05050A)
    val headerBg = Color(0xFF1F0216)
    val textMain = Color(0xFFFFFFFF)
    val textSecondary = Color(0xFFAAAAAA)
    val cardBg = Color(0xFF11111A)
    val successGreen = Color(0xFF28A745)
    val warningYellow = Color(0xFFFFC107)
    val warningBg = Color(0xFFFFF3CD)
    val infoBg = Color(0xFFD1ECF1)
    val infoText = Color(0xFF0C5460)
    
    Scaffold(containerColor = bgMain,
        topBar = {
            TopAppBar(
                title = { Text("Bot WhatsApp", color = textMain, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textMain)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Section
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(successGreen)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Terhubung (62881037160075)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("Terhubung sejak: 9/7/2026, 07.30.20", color = Color.White, fontSize = 12.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { /* action */ },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = successGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, successGreen)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refresh QR")
                        }
                        OutlinedButton(
                            onClick = { /* action */ },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC3545)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC3545))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus Session")
                        }
                    }
                }
            }

            // Warning Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF11111A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PENTING!", color = Color(0xFF856404), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("Gunakan nomor WhatsApp ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("BUKAN nomor admin") }
                                append(" untuk scan QR code ini. Nomor yang digunakan untuk scan akan menjadi bot yang menerima dan memproses pesan WhatsApp.\n\n")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Nomor Admin:") }
                                append(" Hanya untuk mengirim perintah, tidak untuk scan QR code.")
                            },
                            color = Color(0xFF856404),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // WhatsApp Groups Management
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF007BFF), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp Groups Management", color = Color(0xFF007BFF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { /* action */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF17A2B8))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Load Groups")
                        }
                        OutlinedButton(
                            onClick = { /* action */ },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textMain),
                            border = androidx.compose.foundation.BorderStroke(1.dp, textSecondary)
                        ) {
                            Text("Refresh")
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(infoBg)
                            .padding(16.dp)
                    ) {
                        Row {
                            Icon(Icons.Default.Info, contentDescription = null, tint = infoText, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Klik \"Load Groups\" untuk melihat daftar grup WhatsApp yang sudah terkoneksi.", color = infoText, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Commands Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF007BFF), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Perintah Admin WhatsApp", color = Color(0xFF007BFF), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // GenieACS Commands
            item {
                CommandSectionCard(
                    title = "GenieACS",
                    icon = Icons.Default.Dns,
                    iconTint = Color(0xFF28A745),
                    commands = listOf(
                        "devices" to "Daftar perangkat",
                        "cekall" to "Cek semua perangkat",
                        "search [nomor]" to "Cari perangkat",
                        "cek [nomor]" to "Cek status ONU",
                        "cekstatus [nomor]" to "Cek status pelanggan",
                        "admincheck [nomor]" to "Cek perangkat admin",
                        "gantissid [nomor] [ssid]" to "Ubah SSID",
                        "gantipass [nomor] [pass]" to "Ubah password",
                        "reboot [nomor]" to "Restart ONU",
                        "factory reset [nomor]" to "Reset factory",
                        "refresh" to "Refresh data perangkat",
                        "tag [nomor] [tag]" to "Tambah tag pelanggan",
                        "untag [nomor] [tag]" to "Hapus tag",
                        "tags [nomor]" to "Lihat tags",
                        "adminssid [nomor] [ssid]" to "Admin ubah SSID",
                        "adminrestart [nomor]" to "Admin restart ONU",
                        "adminfactory [nomor]" to "Admin factory reset",
                        "confirm admin factory reset [nomor]" to "Konfirmasi factory reset"
                    )
                )
            }

            // PPPoE & Mikrotik Commands
            item {
                CommandSectionCard(
                    title = "PPPoE & Mikrotik",
                    icon = Icons.Default.Router,
                    iconTint = Color(0xFF007BFF),
                    commands = listOf(
                        "interfaces" to "Daftar interface",
                        "interface [nama]" to "Detail interface",
                        "enableif [nama]" to "Aktifkan interface",
                        "disableif [nama]" to "Nonaktifkan interface",
                        "ipaddress" to "Alamat IP",
                        "routes" to "Tabel routing",
                        "dhcp" to "DHCP leases",
                        "ping [ip] [count]" to "Test ping",
                        "logs [topics] [count]" to "Log Mikrotik",
                        "firewall [chain]" to "Status firewall",
                        "users" to "Daftar semua user",
                        "profiles [type]" to "Daftar profile",
                        "identity [nama]" to "Info router",
                        "clock" to "Waktu router",
                        "resource" to "Info resource",
                        "reboot" to "Restart router",
                        "confirm restart" to "Konfirmasi restart",
                        "addtag [device_id] [nomor]" to "Tambah tag perangkat"
                    )
                )
            }

            // Hotspot & PPPoE Management
            item {
                CommandSectionCard(
                    title = "Hotspot & PPPoE Management",
                    icon = Icons.Default.WifiTethering,
                    iconTint = Color(0xFFFFC107),
                    commands = listOf(
                        "vcr [user] [profile] [nomor]" to "Buat voucher",
                        "hotspot" to "User hotspot aktif",
                        "pppoe" to "User PPPoE aktif",
                        "offline" to "User PPPoE offline",
                        "addhotspot [user] [pass] [profile]" to "Tambah user",
                        "addpppoe [user] [pass] [profile] [ip]" to "Tambah PPPoE",
                        "setprofile [user] [profile]" to "Ubah profile",
                        "delhotspot [username]" to "Hapus user hotspot",
                        "delpppoe [username]" to "Hapus user PPPoE",
                        "addpppoe_tag [user] [nomor]" to "Tambah tag PPPoE",
                        "member [username] [profile] [nomor]" to "Tambah member",
                        "list" to "Daftar semua user",
                        "remove [username]" to "Hapus user (generic)",
                        "addadmin [nomor]" to "Tambah nomor admin",
                        "removeadmin [nomor]" to "Hapus nomor admin"
                    )
                )
            }

            // Sistem & Admin Commands
            item {
                CommandSectionCard(
                    title = "Sistem & Admin",
                    icon = Icons.Default.VerifiedUser,
                    iconTint = Color(0xFF17A2B8),
                    commands = listOf(
                        "otp [nomor]" to "Kirim OTP",
                        "status" to "Status sistem",
                        "logs" to "Log aplikasi",
                        "restart" to "Restart aplikasi",
                        "debug resource" to "Debug resource",
                        "checkgroup" to "Cek status group",
                        "setadmin [nomor]" to "Set nomor admin",
                        "settechnician [nomor]" to "Set nomor teknisi",
                        "setheader [teks]" to "Set header pesan",
                        "setfooter [teks]" to "Set footer pesan",
                        "setgenieacs [url] [user] [pass]" to "Set GenieACS",
                        "setmikrotik [host] [port] [user] [pass]" to "Set Mikrotik",
                        "genieacs stop" to "Stop GenieACS",
                        "genieacs start060111" to "Start GenieACS",
                        "admin" to "Menu admin",
                        "help" to "Bantuan perintah",
                        "ya/iya/yes" to "Konfirmasi ya",
                        "tidak/no/batal" to "Konfirmasi tidak",
                        "addwan [interface]" to "Tambah WAN"
                    )
                )
            }

            // WiFi & Layanan Commands
            item {
                CommandSectionCard(
                    title = "WiFi & Layanan",
                    icon = Icons.Default.Wifi,
                    iconTint = Color(0xFF6C757D),
                    commands = listOf(
                        "info wifi" to "Info WiFi pelanggan",
                        "info" to "Info layanan",
                        "gantiwifi [ssid]" to "Ganti nama WiFi",
                        "gantipass [password]" to "Ganti password WiFi",
                        "speedtest" to "Test kecepatan",
                        "diagnostic" to "Diagnostik perangkat",
                        "history" to "Riwayat perangkat",
                        "menu" to "Menu utama",
                        "factory reset" to "Reset factory (pelanggan)",
                        "confirm factory reset" to "Konfirmasi factory reset"
                    )
                )
            }
        }
    }
}

@Composable
fun CommandSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    commands: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, color = iconTint, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                commands.forEach { (cmd, desc) ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = cmd,
                            color = Color(0xFFD81B60),
                            fontSize = 13.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.background(Color(0xFFF8BBD0).copy(alpha = 0.3f)).padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "- $desc",
                            color = Color(0xFF333333),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
