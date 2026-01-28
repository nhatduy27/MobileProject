package com.example.foodapp.pages.shipper.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun HelpScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.InfoLight)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Outlined.HelpOutline,
                    contentDescription = null,
                    tint = ShipperColors.Info,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Bạn cần hỗ trợ? Hãy xem các câu hỏi thường gặp hoặc liên hệ với chúng tôi.",
                    fontSize = 14.sp,
                    color = ShipperColors.TextPrimary,
                    lineHeight = 22.sp
                )
            }
        }
        
        // Contact Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.ContactSupport,
                        contentDescription = null,
                        tint = ShipperColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "Liên hệ hỗ trợ",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = ShipperColors.TextPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                HelpContactItem(Icons.Outlined.Phone, "Hotline", "1900 1234 (8:00 - 22:00)")
                HorizontalDivider(color = ShipperColors.Divider, modifier = Modifier.padding(vertical = 8.dp))
                HelpContactItem(Icons.Outlined.Email, "Email", "support@foodapp.vn")
                HorizontalDivider(color = ShipperColors.Divider, modifier = Modifier.padding(vertical = 8.dp))
                HelpContactItem(Icons.Outlined.Language, "Trung tâm trợ giúp", "https://foodapp.vn/help", ShipperColors.Primary)
                HorizontalDivider(color = ShipperColors.Divider, modifier = Modifier.padding(vertical = 8.dp))
                HelpContactItem(Icons.Outlined.Facebook, "Fanpage", "fb.com/foodapp.vn", ShipperColors.Primary)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary)
        ) {
            Text("Quay lại", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HelpContactItem(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = ShipperColors.TextPrimary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(8.dp),
            color = ShipperColors.PrimaryLight
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = ShipperColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 12.sp,
                color = ShipperColors.TextSecondary
            )
            Text(
                value,
                fontSize = 14.sp,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
