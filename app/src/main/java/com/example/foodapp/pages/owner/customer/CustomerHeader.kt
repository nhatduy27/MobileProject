package com.example.foodapp.pages.owner.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomerHeader(
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Quản lý khách hàng",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Danh sách và lịch sử mua hàng",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E),
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Search - Khi bấm sẽ báo cho CustomerScreen biết để kích hoạt trạng thái tìm kiếm
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Mở thanh tìm kiếm",
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Nút "Thêm mới"
            Surface(
                onClick = onAddClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm khách hàng",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCustomerHeader() {
    CustomerHeader(onAddClick = {}, onSearchClick = {})
}
