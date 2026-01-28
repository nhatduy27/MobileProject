package com.example.foodapp.pages.shipper.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodapp.data.model.shipper.ShipperProfile
import com.example.foodapp.pages.shipper.theme.ShipperColors

@Composable
fun ProfileHeaderSection(
    profile: ShipperProfile,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ShipperColors.Primary)
            .padding(bottom = 24.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = ShipperColors.Surface
                )
            }
            Text(
                text = "Hồ sơ",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShipperColors.Surface
            )
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit",
                    tint = ShipperColors.Surface
                )
            }
        }

        // Profile info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(88.dp),
                shape = CircleShape,
                color = ShipperColors.Surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = profile.name.first().toString(),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ShipperColors.Primary
                    )
                }
            }

            Text(
                text = profile.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = ShipperColors.Surface,
                modifier = Modifier.padding(top = 14.dp)
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = ShipperColors.Surface.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${profile.rating}",
                        fontSize = 14.sp,
                        color = ShipperColors.Surface.copy(alpha = 0.9f)
                    )
                }
                
                Text(
                    text = "•",
                    fontSize = 14.sp,
                    color = ShipperColors.Surface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = "${profile.totalDeliveries} đơn",
                    fontSize = 14.sp,
                    color = ShipperColors.Surface.copy(alpha = 0.9f)
                )
                
                if (profile.isVerified) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = ShipperColors.Surface.copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Verified,
                            contentDescription = null,
                            tint = ShipperColors.Success,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Đã xác thực",
                            fontSize = 14.sp,
                            color = ShipperColors.Success
                        )
                    }
                }
            }
        }
    }
}
