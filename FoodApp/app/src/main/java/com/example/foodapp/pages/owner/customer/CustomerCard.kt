package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.foodapp.data.model.owner.buyer.BuyerListItem
import com.example.foodapp.data.model.owner.buyer.BuyerTier
import com.example.foodapp.pages.owner.theme.OwnerColors
import com.example.foodapp.pages.owner.theme.OwnerDimens

@Composable
fun CustomerCard(
    buyer: BuyerListItem,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = OwnerDimens.CardElevation.dp),
        shape = RoundedCornerShape(OwnerDimens.CardRadiusLarge.dp),
        colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. AVATAR ---
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(buyer.avatar ?: "https://picsum.photos/200")
                    .crossfade(true)
                    .build(),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(OwnerColors.SurfaceVariant)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // --- 2. INFO SECTION ---
            Column(modifier = Modifier.weight(1f)) {
                // Tên + Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = buyer.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OwnerColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(tier = buyer.tier)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Số điện thoại
                if (buyer.phone != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = OwnerColors.TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = buyer.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = OwnerColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Số đơn hàng
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = OwnerColors.TextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${buyer.totalOrders} đơn hàng",
                        style = MaterialTheme.typography.bodySmall,
                        color = OwnerColors.TextSecondary
                    )
                }
            }

            // --- 3. REVENUE (DOANH THU) ---
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = buyer.totalSpentFormatted,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OwnerColors.Primary,
                    fontSize = 17.sp
                )
                Text(
                    text = "Tổng chi",
                    style = MaterialTheme.typography.labelSmall,
                    color = OwnerColors.TextTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// --- COMPONENT CON: BADGE TRẠNG THÁI ---
@Composable
fun StatusBadge(tier: BuyerTier) {
    val (backgroundColor, contentColor, text) = when (tier) {
        BuyerTier.VIP -> Triple(OwnerColors.WarningLight, OwnerColors.Warning, "VIP")
        BuyerTier.NORMAL -> Triple(OwnerColors.SuccessLight, OwnerColors.Success, "Thường xuyên")
        BuyerTier.NEW -> Triple(OwnerColors.InfoLight, OwnerColors.Info, "Mới")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// --- PREVIEWS ---
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun PreviewModernCardVIP() {
    Box(modifier = Modifier.padding(16.dp)) {
        CustomerCard(
            buyer = BuyerListItem(
                customerId = "1",
                displayName = "Nguyễn Phúc Hậu",
                tier = BuyerTier.VIP,
                phone = "0909 123 456",
                totalOrders = 67,
                totalSpent = 3200000.0,
                avatar = "https://picsum.photos/id/1005/200"
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun PreviewModernCardNew() {
    Box(modifier = Modifier.padding(16.dp)) {
        CustomerCard(
            buyer = BuyerListItem(
                customerId = "2",
                displayName = "Trần Thị Bích",
                tier = BuyerTier.NEW,
                phone = "0123 456 789",
                totalOrders = 1,
                totalSpent = 250000.0,
                avatar = "https://picsum.photos/id/1027/200"
            )
        )
    }
}
