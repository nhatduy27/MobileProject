package com.example.foodapp.pages.shipper.earnings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.example.foodapp.R
import com.example.foodapp.pages.shipper.theme.ShipperColors

/**
 * Dialog yêu cầu rút tiền
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoutDialog(
    currentBalance: Long,
    amount: String,
    bankCode: String,
    accountNumber: String,
    accountName: String,
    note: String,
    isLoading: Boolean,
    onAmountChange: (String) -> Unit,
    onBankCodeChange: (String) -> Unit,
    onAccountNumberChange: (String) -> Unit,
    onAccountNameChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    // Danh sách ngân hàng phổ biến
    val banks = listOf(
        "VCB" to "Vietcombank",
        "TCB" to "Techcombank",
        "VPB" to "VPBank",
        "MB" to "MBBank",
        "ACB" to "ACB",
        "BIDV" to "BIDV",
        "VTB" to "VietinBank",
        "TPB" to "TPBank",
        "STB" to "Sacombank",
        "SHB" to "SHB"
    )
    
    var bankExpanded by remember { mutableStateOf(false) }
    val selectedBank = banks.find { it.first == bankCode }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.shipper_payout_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ShipperColors.TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.shipper_payout_close),
                            tint = ShipperColors.TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Số dư hiện tại
                Text(
                    text = stringResource(R.string.shipper_payout_current_balance, String.format("%,d", currentBalance) + "đ"),
                    fontSize = 14.sp,
                    color = ShipperColors.Primary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Số tiền rút
                OutlinedTextField(
                    value = amount,
                    onValueChange = { value ->
                        // Chỉ cho phép số
                        if (value.all { it.isDigit() }) {
                            onAmountChange(value)
                        }
                    },
                    label = { Text(stringResource(R.string.shipper_payout_amount)) },
                    placeholder = { Text(stringResource(R.string.shipper_payout_min_amount)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShipperColors.Primary,
                        unfocusedBorderColor = ShipperColors.BorderLight
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Chọn ngân hàng
                ExposedDropdownMenuBox(
                    expanded = bankExpanded,
                    onExpandedChange = { bankExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedBank?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.shipper_payout_bank)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ShipperColors.Primary,
                            unfocusedBorderColor = ShipperColors.BorderLight
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = bankExpanded,
                        onDismissRequest = { bankExpanded = false }
                    ) {
                        banks.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    onBankCodeChange(code)
                                    bankExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Số tài khoản
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) {
                            onAccountNumberChange(value)
                        }
                    },
                    label = { Text(stringResource(R.string.shipper_payout_account_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShipperColors.Primary,
                        unfocusedBorderColor = ShipperColors.BorderLight
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tên chủ tài khoản
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { onAccountNameChange(it.uppercase()) },
                    label = { Text(stringResource(R.string.shipper_payout_account_name)) },
                    placeholder = { Text(stringResource(R.string.shipper_payout_account_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShipperColors.Primary,
                        unfocusedBorderColor = ShipperColors.BorderLight
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Ghi chú (optional)
                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChange,
                    label = { Text(stringResource(R.string.shipper_payout_note)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ShipperColors.Primary,
                        unfocusedBorderColor = ShipperColors.BorderLight
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Nút gửi yêu cầu
                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading && amount.isNotEmpty() && bankCode.isNotEmpty() 
                            && accountNumber.isNotEmpty() && accountName.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ShipperColors.Surface,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.shipper_payout_submit),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Lưu ý
                Text(
                    text = stringResource(R.string.shipper_payout_processing_note),
                    fontSize = 12.sp,
                    color = ShipperColors.TextTertiary
                )
            }
        }
    }
}
