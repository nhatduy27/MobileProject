package com.example.foodapp.pages.owner.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.foodapp.R
import com.example.foodapp.data.model.owner.wallet.RequestPayoutRequest

/**
 * Dialog for requesting payout (withdraw funds)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayoutDialog(
    currentBalance: Double,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (RequestPayoutRequest) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var bankCode by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    var amountError by remember { mutableStateOf<String?>(null) }
    var bankCodeError by remember { mutableStateOf<String?>(null) }
    var accountNumberError by remember { mutableStateOf<String?>(null) }
    var accountNameError by remember { mutableStateOf<String?>(null) }
    
    val currencySuffix = stringResource(R.string.owner_currency_suffix)
    val formattedBalance = String.format("%,.0f", currentBalance) + currencySuffix
    
    // Error messages
    val errorMinAmount = stringResource(R.string.wallet_payout_error_min_amount)
    val errorInsufficient = stringResource(R.string.wallet_payout_error_insufficient)
    val errorBankCode = stringResource(R.string.wallet_payout_error_bank_code)
    val errorAccountNumber = stringResource(R.string.wallet_payout_error_account_number)
    val errorAccountName = stringResource(R.string.wallet_payout_error_account_name)
    
    // Validation
    fun validate(): Boolean {
        var isValid = true
        
        val amountValue = amount.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
        if (amountValue < 100000) {
            amountError = errorMinAmount
            isValid = false
        } else if (amountValue > currentBalance) {
            amountError = errorInsufficient
            isValid = false
        } else {
            amountError = null
        }
        
        if (bankCode.isBlank()) {
            bankCodeError = errorBankCode
            isValid = false
        } else {
            bankCodeError = null
        }
        
        if (accountNumber.isBlank()) {
            accountNumberError = errorAccountNumber
            isValid = false
        } else {
            accountNumberError = null
        }
        
        if (accountName.isBlank()) {
            accountNameError = errorAccountName
            isValid = false
        } else {
            accountNameError = null
        }
        
        return isValid
    }
    
    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.wallet_withdraw_request),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(
                        onClick = { if (!isLoading) onDismiss() },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.owner_close))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Balance info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.wallet_balance),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedBalance,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Amount field
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it.filter { char -> char.isDigit() }
                        amountError = null
                    },
                    label = { Text(stringResource(R.string.wallet_payout_amount)) },
                    placeholder = { Text(stringResource(R.string.wallet_payout_amount_hint)) },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    enabled = !isLoading,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bank code field
                OutlinedTextField(
                    value = bankCode,
                    onValueChange = { 
                        bankCode = it.uppercase()
                        bankCodeError = null
                    },
                    label = { Text(stringResource(R.string.wallet_payout_bank_code)) },
                    placeholder = { Text(stringResource(R.string.wallet_payout_bank_code_hint)) },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = bankCodeError != null,
                    supportingText = bankCodeError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    enabled = !isLoading,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Account number field
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { 
                        accountNumber = it.filter { char -> char.isDigit() }
                        accountNumberError = null
                    },
                    label = { Text(stringResource(R.string.wallet_payout_account_number)) },
                    placeholder = { Text(stringResource(R.string.wallet_payout_account_number_hint)) },
                    leadingIcon = { Icon(Icons.Default.CreditCard, null) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = accountNumberError != null,
                    supportingText = accountNumberError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    enabled = !isLoading,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Account name field
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { 
                        accountName = it.uppercase()
                        accountNameError = null
                    },
                    label = { Text(stringResource(R.string.wallet_payout_account_name)) },
                    placeholder = { Text(stringResource(R.string.wallet_payout_account_name_hint)) },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = accountNameError != null,
                    supportingText = accountNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    enabled = !isLoading,
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Note field (optional)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.wallet_payout_note)) },
                    placeholder = { Text(stringResource(R.string.wallet_payout_note_hint)) },
                    leadingIcon = { Icon(Icons.Default.Note, null) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Info text
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.wallet_payout_info),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF795548)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text(stringResource(R.string.owner_cancel))
                    }
                    
                    Button(
                        onClick = {
                            if (validate()) {
                                val amountValue = amount.replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
                                onSubmit(RequestPayoutRequest(
                                    amount = amountValue,
                                    bankCode = bankCode,
                                    accountNumber = accountNumber,
                                    accountName = accountName,
                                    note = note.ifBlank { null }
                                ))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.wallet_payout_submit))
                        }
                    }
                }
            }
        }
    }
}
