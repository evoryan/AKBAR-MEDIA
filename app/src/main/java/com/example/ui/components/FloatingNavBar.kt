package com.example.ui.components

import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingNavBar(
    currentRoute: String?,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToBilling: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val isDark = androidx.compose.material3.MaterialTheme.colorScheme.background.luminance() < 0.5f
    val navBg = if(isDark) androidx.compose.ui.graphics.Color(0xFF11111A) else androidx.compose.ui.graphics.Color(0xFFFFFFFF)
    val navBorder = if(isDark) androidx.compose.ui.graphics.Color(0xFF00FFFF).copy(alpha = 0.3f) else androidx.compose.ui.graphics.Color(0xFF0066FF).copy(alpha = 0.3f)
    val primaryContainer = if(isDark) androidx.compose.ui.graphics.Color(0xFF00FFFF).copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color(0xFF0066FF).copy(alpha = 0.15f)
    val onPrimaryContainer = if(isDark) androidx.compose.ui.graphics.Color(0xFF00FFFF) else androidx.compose.ui.graphics.Color(0xFF0066FF)
    val textSecondary = if(isDark) androidx.compose.ui.graphics.Color(0xFFAAAAAA) else androidx.compose.ui.graphics.Color(0xFF666666)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(36.dp))
                .background(navBg)
                .border(1.dp, navBorder, RoundedCornerShape(36.dp))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(
                icon = Icons.Default.Dashboard,
                label = "Beranda",
                isSelected = currentRoute?.contains("DashboardRoute") == true || currentRoute?.contains("NocDashboardRoute") == true,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                textSecondary = textSecondary,
                onClick = onNavigateToDashboard
            )
            NavItem(
                icon = Icons.Default.ReceiptLong,
                label = "Tagihan",
                isSelected = currentRoute?.contains("BillingRoute") == true,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                textSecondary = textSecondary,
                onClick = onNavigateToBilling
            )
            NavItem(
                icon = Icons.Default.PersonSearch,
                label = "Pelanggan",
                isSelected = currentRoute?.contains("CustomersRoute") == true || currentRoute?.contains("CustomerDetailRoute") == true || currentRoute?.contains("AddCustomerRoute") == true,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                textSecondary = textSecondary,
                onClick = onNavigateToCustomers
            )
            NavItem(
                icon = Icons.Default.Settings,
                label = "Setting",
                isSelected = currentRoute?.contains("Settings") == true,
                primaryContainer = primaryContainer,
                onPrimaryContainer = onPrimaryContainer,
                textSecondary = textSecondary,
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    primaryContainer: Color,
    onPrimaryContainer: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(primaryContainer)
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = onPrimaryContainer)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = onPrimaryContainer)
        } else {
            Icon(icon, contentDescription = label, tint = textSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontWeight = FontWeight.Medium, fontSize = 10.sp, color = textSecondary)
        }
    }
}
