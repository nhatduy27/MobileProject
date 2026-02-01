package com.example.foodapp.pages.client.components.payment

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodapp.R
import com.example.foodapp.data.remote.client.response.voucher.VoucherApiModel
import com.example.foodapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherDropdown(
    vouchers: List<VoucherApiModel>,
    selectedVoucher: VoucherApiModel?,
    isLoading: Boolean = false,
    isValidationLoading: Boolean = false,
    validationError: String? = null,
    onVoucherSelected: (VoucherApiModel?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "rotation"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header với icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = stringResource(id = R.string.voucher_info_icon),
                        tint = PrimaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.voucher_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                if (isLoading || isValidationLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = PrimaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dropdown field với style tốt hơn
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    if (!isLoading && !isValidationLoading) {
                        expanded = !expanded
                    }
                }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (expanded) PrimaryColor.copy(alpha = 0.05f) else Color(0xFFF8F9FA),
                    border = if (expanded) {
                        androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryColor)
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (selectedVoucher != null) {
                                if (isValidationLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = PrimaryColor
                                    )
                                } else if (validationError != null) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = stringResource(id = R.string.voucher_error_icon),
                                        tint = ErrorColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = stringResource(id = R.string.voucher_check_icon),
                                        tint = SuccessColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = selectedVoucher?.code ?: stringResource(id = R.string.voucher_placeholder),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedVoucher != null) {
                                    if (validationError != null) ErrorColor else TextPrimary
                                } else TextSecondary,
                                fontWeight = if (selectedVoucher != null) FontWeight.Medium else FontWeight.Normal
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.voucher_arrow_down_icon),
                            tint = if (expanded) PrimaryColor else TextSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(rotationAngle)
                        )
                    }
                }

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    // Option: Không chọn voucher
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.voucher_close_icon),
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    stringResource(id = R.string.voucher_no_selected_option),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        onClick = {
                            onVoucherSelected(null)
                            expanded = false
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = TextSecondary
                        )
                    )

                    if (vouchers.isNotEmpty()) {
                        HorizontalDivider(thickness = 1.dp, color = BorderColor)
                    }

                    if (vouchers.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = stringResource(id = R.string.voucher_info_icon),
                                            tint = TextSecondary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            stringResource(id = R.string.voucher_no_available),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        vouchers.forEach { voucher ->
                            DropdownMenuItem(
                                text = {
                                    VoucherDropdownItem(voucher = voucher)
                                },
                                onClick = {
                                    onVoucherSelected(voucher)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Hiển thị thông báo lỗi validation nếu có
            validationError?.let { error ->
                AnimatedVisibility(
                    visible = error.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ErrorColor.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ErrorColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(id = R.string.voucher_warning_icon),
                                tint = ErrorColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = ErrorColor,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onVoucherSelected(null) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.voucher_deselect),
                                    tint = ErrorColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Hiển thị thông tin voucher đã chọn với animation
            AnimatedVisibility(
                visible = selectedVoucher != null && validationError == null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedVoucher?.let { voucher ->
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        VoucherInfoCard(voucher = voucher)
                    }
                }
            }
        }
    }
}

@Composable
private fun VoucherDropdownItem(voucher: VoucherApiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = voucher.code,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = voucher.name,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = SuccessColor.copy(alpha = 0.1f),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text(
                text = when (voucher.type.uppercase()) {
                    "PERCENTAGE" -> stringResource(id = R.string.voucher_discount_percentage, voucher.value.toInt())
                    "FIXED" -> stringResource(id = R.string.voucher_discount_fixed, formatCurrency(voucher.value))
                    else -> ""
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = SuccessColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun VoucherInfoCard(
    voucher: VoucherApiModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryColor.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header với code và giá trị
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryColor,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = stringResource(id = R.string.voucher_info_icon),
                            tint = Color.White,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = voucher.code,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryColor
                        )
                        Text(
                            text = voucher.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessColor,
                    modifier = Modifier
                ) {
                    Text(
                        text = when (voucher.type.uppercase()) {
                            "PERCENTAGE" -> stringResource(id = R.string.voucher_discount_percentage, voucher.value.toInt())
                            "FIXED" -> stringResource(id = R.string.voucher_discount_fixed, formatCurrency(voucher.value))
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Description
            voucher.description?.let { description ->
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            // Footer info
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = BorderColor.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = stringResource(id = R.string.product_shopping_bag_icon),
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.voucher_min_order, formatCurrency(voucher.minOrderAmount)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(id = R.string.voucher_remaining_uses),
                        tint = InfoColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.voucher_remaining_uses, voucher.myRemainingUses),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = InfoColor
                    )
                }
            }
        }
    }
}

private fun formatCurrency(amount: Double): String {
    return String.format("%,.0f", amount) + "đ"
}