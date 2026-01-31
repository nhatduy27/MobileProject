package com.example.foodapp.pages.shipper.removal

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.foodapp.R
import com.example.foodapp.data.di.RepositoryProvider
import com.example.foodapp.data.model.shipper.removal.*
import com.example.foodapp.data.repository.shipper.base.RemovalRequestRepository
import com.example.foodapp.pages.shipper.theme.ShipperColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ==================== UI STATE ====================

data class ApprovedShop(
    val shopId: String,
    val shopName: String
)

data class RemovalRequestUiState(
    val requests: List<RemovalRequest> = emptyList(),
    val approvedShops: List<ApprovedShop> = emptyList(), // Shops shipper is working for
    val selectedShop: ApprovedShop? = null,
    val isLoading: Boolean = false,
    val isLoadingShops: Boolean = false,
    val isCreating: Boolean = false,
    val statusFilter: RemovalRequestStatus? = null,
    val showCreateDialog: Boolean = false,
    val selectedType: RemovalRequestType = RemovalRequestType.TRANSFER,
    val reason: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

// ==================== VIEW MODEL ====================

class RemovalRequestViewModel : ViewModel() {
    
    private val repository: RemovalRequestRepository = RepositoryProvider.getRemovalRequestRepository()
    private val applicationRepository = RepositoryProvider.getShipperApplicationRepository()
    
    private val _uiState = MutableStateFlow(RemovalRequestUiState())
    val uiState: StateFlow<RemovalRequestUiState> = _uiState.asStateFlow()
    
    init {
        loadApprovedShops()
        loadRequests()
    }
    
    /**
     * Load shops shipper is currently working for (from approved applications)
     */
    private fun loadApprovedShops() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingShops = true) }
            
            applicationRepository.getMyApplications()
                .onSuccess { applications ->
                    val approvedShops = applications
                        .filter { it.status == "APPROVED" }
                        .map { ApprovedShop(it.shopId, it.shopName ?: "Shop #${it.shopId.take(8)}") }
                    
                    _uiState.update { 
                        it.copy(
                            approvedShops = approvedShops,
                            isLoadingShops = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(isLoadingShops = false)
                    }
                }
        }
    }
    
    fun loadRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.getMyRemovalRequests(_uiState.value.statusFilter)
                .onSuccess { requests ->
                    _uiState.update { 
                        it.copy(
                            requests = requests,
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }
    
    fun setStatusFilter(status: RemovalRequestStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
        loadRequests()
    }
    
    fun showCreateDialog() {
        val shops = _uiState.value.approvedShops
        _uiState.update { 
            it.copy(
                showCreateDialog = true,
                selectedShop = shops.firstOrNull(),
                selectedType = RemovalRequestType.TRANSFER,
                reason = ""
            )
        }
    }
    
    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }
    
    fun selectShop(shop: ApprovedShop) {
        _uiState.update { it.copy(selectedShop = shop) }
    }
    
    fun setType(type: RemovalRequestType) {
        _uiState.update { it.copy(selectedType = type) }
    }
    
    fun setReason(reason: String) {
        _uiState.update { it.copy(reason = reason) }
    }
    
    fun createRequest() {
        val state = _uiState.value
        val selectedShop = state.selectedShop
        
        if (selectedShop == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn shop") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            
            val dto = CreateRemovalRequestDto(
                shopId = selectedShop.shopId,
                type = state.selectedType.name,
                reason = state.reason.takeIf { it.isNotBlank() }
            )
            
            repository.createRemovalRequest(dto)
                .onSuccess { request ->
                    _uiState.update { 
                        it.copy(
                            isCreating = false,
                            showCreateDialog = false,
                            successMessage = "Đã gửi yêu cầu ${request.getTypeDisplayName().lowercase()}"
                        )
                    }
                    loadRequests()
                }
                .onFailure { e ->
                    _uiState.update { 
                        it.copy(
                            isCreating = false,
                            errorMessage = e.message
                        )
                    }
                }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

// ==================== SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemovalRequestScreen(
    onBack: () -> Unit = {},
    showTopBar: Boolean = true,
    viewModel: RemovalRequestViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Handle messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, "Lỗi: $message", Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearSuccess()
        }
    }
    
    // Create Dialog
    if (uiState.showCreateDialog) {
        CreateRemovalRequestDialog(
            approvedShops = uiState.approvedShops,
            selectedShop = uiState.selectedShop,
            selectedType = uiState.selectedType,
            reason = uiState.reason,
            isCreating = uiState.isCreating,
            onShopSelect = { viewModel.selectShop(it) },
            onTypeChange = { viewModel.setType(it) },
            onReasonChange = { viewModel.setReason(it) },
            onDismiss = { viewModel.dismissCreateDialog() },
            onCreate = { viewModel.createRequest() }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShipperColors.Background)
    ) {
        // Top Bar (optional)
        if (showTopBar) {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.shipper_removal_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.shipper_removal_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ShipperColors.Surface
                )
            )
        }
        
        // Header with create button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(R.string.shipper_removal_list),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ShipperColors.TextPrimary
                )
                Text(
                    stringResource(R.string.shipper_removal_count, uiState.requests.size),
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary
                )
            }
            
            // Show create button if shipper is working at any shop
            if (uiState.approvedShops.isNotEmpty()) {
                Button(
                    onClick = { viewModel.showCreateDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = ShipperColors.Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.shipper_removal_create))
                }
            }
        }
        
        // Info card if no shops
        if (uiState.approvedShops.isEmpty() && !uiState.isLoadingShops) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = ShipperColors.InfoLight),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = ShipperColors.Info,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.shipper_removal_no_shop),
                        fontSize = 13.sp,
                        color = ShipperColors.Info
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Filter Tabs
        FilterTabs(
            selectedStatus = uiState.statusFilter,
            onStatusChange = { viewModel.setStatusFilter(it) }
        )
        
        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ShipperColors.Primary)
            }
        } else if (uiState.requests.isEmpty()) {
            EmptyRequestsView(
                onRefresh = { viewModel.loadRequests() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.requests) { request ->
                    RemovalRequestCard(request = request)
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun FilterTabs(
    selectedStatus: RemovalRequestStatus?,
    onStatusChange: (RemovalRequestStatus?) -> Unit
) {
    val tabs = listOf(
        null to stringResource(R.string.shipper_removal_filter_all),
        RemovalRequestStatus.PENDING to stringResource(R.string.shipper_removal_filter_pending),
        RemovalRequestStatus.APPROVED to stringResource(R.string.shipper_removal_filter_approved),
        RemovalRequestStatus.REJECTED to stringResource(R.string.shipper_removal_filter_rejected)
    )
    
    ScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedStatus }.coerceAtLeast(0),
        containerColor = ShipperColors.Surface,
        contentColor = ShipperColors.Primary,
        edgePadding = 16.dp
    ) {
        tabs.forEach { (status, label) ->
            Tab(
                selected = selectedStatus == status,
                onClick = { onStatusChange(status) },
                text = {
                    Text(
                        label,
                        fontWeight = if (selectedStatus == status) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRemovalRequestDialog(
    approvedShops: List<ApprovedShop>,
    selectedShop: ApprovedShop?,
    selectedType: RemovalRequestType,
    reason: String,
    isCreating: Boolean,
    onShopSelect: (ApprovedShop) -> Unit,
    onTypeChange: (RemovalRequestType) -> Unit,
    onReasonChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    var showShopDropdown by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = { 
            Text(
                stringResource(R.string.shipper_removal_dialog_title),
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Shop selection (if multiple shops)
                if (approvedShops.size > 1) {
                    Text(
                        stringResource(R.string.shipper_removal_select_shop_label),
                        fontWeight = FontWeight.Medium,
                        color = ShipperColors.TextPrimary
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = showShopDropdown,
                        onExpandedChange = { showShopDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedShop?.shopName ?: stringResource(R.string.shipper_removal_select_shop),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showShopDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            enabled = !isCreating
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showShopDropdown,
                            onDismissRequest = { showShopDropdown = false }
                        ) {
                            approvedShops.forEach { shop ->
                                DropdownMenuItem(
                                    text = { Text(shop.shopName) },
                                    onClick = {
                                        onShopSelect(shop)
                                        showShopDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else if (selectedShop != null) {
                    // Single shop - just display it
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ShipperColors.SurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Store,
                                contentDescription = null,
                                tint = ShipperColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                selectedShop.shopName,
                                fontWeight = FontWeight.Medium,
                                color = ShipperColors.TextPrimary
                            )
                        }
                    }
                }
                
                // Type selection
                Text(
                    stringResource(R.string.shipper_removal_type_label),
                    fontWeight = FontWeight.Medium,
                    color = ShipperColors.TextPrimary
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RemovalRequestType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { onTypeChange(type) },
                            label = {
                                Text(
                                    when (type) {
                                        RemovalRequestType.QUIT -> stringResource(R.string.shipper_removal_type_quit)
                                        RemovalRequestType.TRANSFER -> stringResource(R.string.shipper_removal_type_transfer)
                                    }
                                )
                            },
                            leadingIcon = if (selectedType == type) {
                                {
                                    Icon(
                                        Icons.Outlined.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
                
                // Type description
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShipperColors.InfoLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (selectedType) {
                            RemovalRequestType.QUIT -> stringResource(R.string.shipper_removal_quit_desc)
                            RemovalRequestType.TRANSFER -> stringResource(R.string.shipper_removal_transfer_desc)
                        },
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = ShipperColors.Info
                    )
                }
                
                // Reason input
                OutlinedTextField(
                    value = reason,
                    onValueChange = onReasonChange,
                    label = { Text(stringResource(R.string.shipper_removal_reason_label)) },
                    placeholder = { Text(stringResource(R.string.shipper_removal_reason_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    enabled = !isCreating
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = !isCreating && selectedShop != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == RemovalRequestType.QUIT) 
                        ShipperColors.Error else ShipperColors.Primary
                )
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.shipper_removal_submit))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isCreating
            ) {
                Text(stringResource(R.string.shipper_cancel))
            }
        }
    )
}

@Composable
private fun RemovalRequestCard(request: RemovalRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ShipperColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shop name
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Store,
                        contentDescription = null,
                        tint = ShipperColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        request.shopName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = ShipperColors.TextPrimary
                    )
                }
                
                // Status badge
                StatusBadge(status = request.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Type
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (request.type) {
                        RemovalRequestType.QUIT -> Icons.Outlined.ExitToApp
                        RemovalRequestType.TRANSFER -> Icons.Outlined.SwapHoriz
                    },
                    contentDescription = null,
                    tint = ShipperColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    request.getTypeDisplayName(),
                    fontSize = 14.sp,
                    color = ShipperColors.TextSecondary
                )
            }
            
            // Reason
            if (!request.reason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Lý do: ${request.reason}",
                    fontSize = 13.sp,
                    color = ShipperColors.TextSecondary
                )
            }
            
            // Rejection reason
            if (request.status == RemovalRequestStatus.REJECTED && !request.rejectionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = ShipperColors.ErrorLight),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Lý do từ chối: ${request.rejectionReason}",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = ShipperColors.Error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Ngày tạo: ${formatDate(request.createdAt)}",
                    fontSize = 12.sp,
                    color = ShipperColors.TextTertiary
                )
                
                if (request.processedAt != null) {
                    Text(
                        "Xử lý: ${formatDate(request.processedAt)}",
                        fontSize = 12.sp,
                        color = ShipperColors.TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: RemovalRequestStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        RemovalRequestStatus.PENDING -> Triple(ShipperColors.WarningLight, ShipperColors.Warning, stringResource(R.string.shipper_removal_status_pending))
        RemovalRequestStatus.APPROVED -> Triple(ShipperColors.SuccessLight, ShipperColors.Success, stringResource(R.string.shipper_removal_status_approved))
        RemovalRequestStatus.REJECTED -> Triple(ShipperColors.ErrorLight, ShipperColors.Error, stringResource(R.string.shipper_removal_status_rejected))
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun EmptyRequestsView(onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.Assignment,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = ShipperColors.TextTertiary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            stringResource(R.string.shipper_removal_empty),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = ShipperColors.TextSecondary
        )
        
        Text(
            stringResource(R.string.shipper_removal_empty_desc),
            fontSize = 14.sp,
            color = ShipperColors.TextTertiary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onRefresh,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ShipperColors.Primary)
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.shipper_removal_refresh))
        }
    }
}

// Helper function
private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(10)
    }
}
