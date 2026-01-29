package com.example.foodapp.pages.owner.vouchers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.foodapp.data.model.owner.voucher.*
import com.example.foodapp.pages.owner.theme.OwnerColors
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Dialog to create a new voucher
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoucherDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateVoucherRequest) -> Unit,
    isLoading: Boolean
) {
    var code by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(VoucherType.PERCENTAGE) }
    var value by remember { mutableStateOf("") }
    var maxDiscount by remember { mutableStateOf("") }
    var minOrderAmount by remember { mutableStateOf("") }
    var usageLimit by remember { mutableStateOf("100") }
    var usageLimitPerUser by remember { mutableStateOf("1") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Date pickers
    var validFrom by remember { mutableStateOf(System.currentTimeMillis()) }
    var validTo by remember { mutableStateOf(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000) } // +30 days
    
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }
    
    var codeError by remember { mutableStateOf<String?>(null) }
    var valueError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Tạo Voucher Mới",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OwnerColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Code
                    OutlinedTextField(
                        value = code,
                        onValueChange = { 
                            code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(10)
                            codeError = null
                        },
                        label = { Text("Mã voucher (6-10 ký tự)") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = codeError != null,
                        supportingText = codeError?.let { { Text(it, color = Color.Red) } },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Type selector
                    Text(
                        text = "Loại voucher",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = OwnerColors.TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoucherType.entries.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type.displayName(), fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OwnerColors.Primary,
                                    selectedLabelColor = OwnerColors.Surface
                                )
                            )
                        }
                    }

                    // Value
                    OutlinedTextField(
                        value = value,
                        onValueChange = { 
                            value = it.filter { c -> c.isDigit() || c == '.' }
                            valueError = null
                        },
                        label = { 
                            Text(
                                when (selectedType) {
                                    VoucherType.PERCENTAGE -> "Phần trăm giảm (%)"
                                    VoucherType.FIXED_AMOUNT -> "Số tiền giảm (VNĐ)"
                                    VoucherType.FREE_SHIP -> "Phần trăm giảm ship (%)"
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = valueError != null,
                        supportingText = valueError?.let { { Text(it, color = Color.Red) } },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Max discount (only for PERCENTAGE)
                    if (selectedType == VoucherType.PERCENTAGE) {
                        OutlinedTextField(
                            value = maxDiscount,
                            onValueChange = { maxDiscount = it.filter { c -> c.isDigit() } },
                            label = { Text("Giảm tối đa (VNĐ) *") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Min order amount
                    OutlinedTextField(
                        value = minOrderAmount,
                        onValueChange = { minOrderAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Đơn tối thiểu (VNĐ)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Usage limits
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = usageLimit,
                            onValueChange = { usageLimit = it.filter { c -> c.isDigit() } },
                            label = { Text("Tổng lượt dùng") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = usageLimitPerUser,
                            onValueChange = { usageLimitPerUser = it.filter { c -> c.isDigit() } },
                            label = { Text("Mỗi người") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Date pickers
                    Text(
                        text = "Thời hạn hiệu lực",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = OwnerColors.TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFromDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Từ: ${formatDate(validFrom)}", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { showToDatePicker = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Đến: ${formatDate(validTo)}", fontSize = 12.sp)
                        }
                    }

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(100) },
                        label = { Text("Tên voucher (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it.take(500) },
                        label = { Text("Mô tả (tùy chọn)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = {
                            // Validate
                            var hasError = false
                            
                            if (code.length < 6 || code.length > 10) {
                                codeError = "Mã phải từ 6-10 ký tự"
                                hasError = true
                            }
                            
                            val valueNum = value.toDoubleOrNull()
                            if (valueNum == null || valueNum <= 0) {
                                valueError = "Giá trị không hợp lệ"
                                hasError = true
                            } else if ((selectedType == VoucherType.PERCENTAGE || selectedType == VoucherType.FREE_SHIP) && valueNum > 100) {
                                valueError = "Phần trăm không được vượt quá 100"
                                hasError = true
                            }
                            
                            if (hasError) return@Button

                            val request = CreateVoucherRequest(
                                code = code,
                                type = selectedType.name,
                                value = valueNum!!,
                                maxDiscount = if (selectedType == VoucherType.PERCENTAGE) maxDiscount.toDoubleOrNull() else null,
                                minOrderAmount = minOrderAmount.toDoubleOrNull(),
                                usageLimit = usageLimit.toIntOrNull() ?: 100,
                                usageLimitPerUser = usageLimitPerUser.toIntOrNull() ?: 1,
                                validFrom = formatIsoDate(validFrom),
                                validTo = formatIsoDate(validTo),
                                name = name.ifBlank { null },
                                description = description.ifBlank { null }
                            )
                            onCreate(request)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Primary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Tạo voucher")
                        }
                    }
                }
            }
        }
    }

    // Date pickers
    if (showFromDatePicker) {
        DatePickerModal(
            selectedDate = validFrom,
            onDateSelected = { 
                if (it != null) validFrom = it
                showFromDatePicker = false
            },
            onDismiss = { showFromDatePicker = false }
        )
    }

    if (showToDatePicker) {
        DatePickerModal(
            selectedDate = validTo,
            onDateSelected = { 
                if (it != null) validTo = it
                showToDatePicker = false
            },
            onDismiss = { showToDatePicker = false }
        )
    }
}

/**
 * Dialog to edit an existing voucher (limited fields)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVoucherDialog(
    voucher: Voucher,
    onDismiss: () -> Unit,
    onSave: (UpdateVoucherRequest) -> Unit,
    isLoading: Boolean
) {
    var usageLimit by remember { mutableStateOf(voucher.usageLimit.toString()) }
    var usageLimitPerUser by remember { mutableStateOf(voucher.usageLimitPerUser.toString()) }
    var name by remember { mutableStateOf(voucher.name ?: "") }
    var description by remember { mutableStateOf(voucher.description ?: "") }
    
    // Parse existing validTo
    val initialValidTo = try {
        Instant.parse(voucher.validTo).toEpochMilli()
    } catch (e: Exception) {
        System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
    }
    var validTo by remember { mutableStateOf(initialValidTo) }
    var showToDatePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = OwnerColors.Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = "Chỉnh sửa Voucher",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OwnerColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Voucher code display
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OwnerColors.Primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = voucher.code,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = OwnerColors.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Lưu ý: Chỉ có thể sửa một số trường nhất định",
                    fontSize = 12.sp,
                    color = OwnerColors.TextTertiary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Usage limits
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = usageLimit,
                            onValueChange = { usageLimit = it.filter { c -> c.isDigit() } },
                            label = { Text("Tổng lượt dùng") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = usageLimitPerUser,
                            onValueChange = { usageLimitPerUser = it.filter { c -> c.isDigit() } },
                            label = { Text("Mỗi người") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Valid to date
                    Text(
                        text = "Ngày hết hạn",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = OwnerColors.TextSecondary
                    )
                    OutlinedButton(
                        onClick = { showToDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hết hạn: ${formatDate(validTo)}")
                    }

                    // Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(100) },
                        label = { Text("Tên voucher") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it.take(500) },
                        label = { Text("Mô tả") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        maxLines = 3
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("Hủy")
                    }
                    Button(
                        onClick = {
                            val request = UpdateVoucherRequest(
                                usageLimit = usageLimit.toIntOrNull(),
                                usageLimitPerUser = usageLimitPerUser.toIntOrNull(),
                                validTo = formatIsoDate(validTo),
                                name = name.ifBlank { null },
                                description = description.ifBlank { null }
                            )
                            onSave(request)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Primary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Lưu thay đổi")
                        }
                    }
                }
            }
        }
    }

    // Date picker
    if (showToDatePicker) {
        DatePickerModal(
            selectedDate = validTo,
            onDateSelected = { 
                if (it != null) validTo = it
                showToDatePicker = false
            },
            onDismiss = { showToDatePicker = false }
        )
    }
}

/**
 * Dialog to confirm voucher deletion
 */
@Composable
fun DeleteVoucherDialog(
    voucher: Voucher,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Xóa voucher") },
        text = {
            Column {
                Text("Bạn có chắc chắn muốn xóa voucher này?")
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OwnerColors.Primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = voucher.code,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = OwnerColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (!voucher.name.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = voucher.name,
                        fontSize = 14.sp,
                        color = OwnerColors.TextSecondary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = OwnerColors.Error)
            ) {
                Text("Xóa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

/**
 * Date picker modal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    selectedDate: Long,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
                Text("Chọn")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

// Helper functions
private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatIsoDate(millis: Long): String {
    val instant = Instant.ofEpochMilli(millis)
    val formatter = DateTimeFormatter.ISO_INSTANT
    return formatter.format(instant)
}
