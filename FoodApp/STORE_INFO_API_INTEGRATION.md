# ✅ Tích hợp API Backend vào "Thông tin cửa hàng"

## 📋 Tổng kết

Đã thành công tích hợp API backend vào màn hình **"Thông tin cửa hàng"** trong phần Cài đặt của Owner.

---

## 🔄 Thay đổi đã thực hiện

### 1. **Thay thế màn hình cũ**
- **File cũ**: `StoreInfoScreen.kt` (hardcoded data, không có API)
- **File mới**: `ShopManagementScreen.kt` (đầy đủ API integration)

### 2. **Cập nhật Navigation**
**File**: `SettingsNavHost.kt`

**Trước:**
```kotlin
composable("store_info") {
    StoreInfoScreen(navController = navController)
}
```

**Sau:**
```kotlin
composable("store_info") {
    com.example.foodapp.pages.owner.shopmanagement.ShopManagementScreen()
}
```

### 3. **Thêm Back Button**
**File**: `ShopManagementScreen.kt`

Thêm `navigationIcon` vào TopAppBar để user có thể quay lại màn hình Settings:
```kotlin
navigationIcon = {
    val navController = LocalNavController.current
    IconButton(onClick = { navController?.navigateUp() }) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Quay lại",
            tint = Color.White
        )
    }
}
```

---

## 🎯 Tính năng hiện có

### ✅ **Load dữ liệu từ Backend**
Khi mở màn hình, `ShopManagementViewModel` tự động:
1. Gọi `GET /owner/shop` để lấy thông tin shop
2. Hiển thị loading indicator
3. Fill form với dữ liệu từ backend
4. Hiển thị ảnh hiện tại (coverImage, logo)

**Code trong ViewModel:**
```kotlin
init {
    loadShopData()
}

private fun loadShopData() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        
        val result = repository.getMyShop()
        
        result.onSuccess { shop ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    shopName = shop.name,
                    description = shop.description,
                    address = shop.address,
                    phone = shop.phone,
                    openTime = shop.openTime,
                    closeTime = shop.closeTime,
                    shipFee = shop.shipFeePerOrder.toString(),
                    minOrderAmount = shop.minOrderAmount.toString(),
                    coverImageUrl = shop.coverImageUrl ?: "",
                    logoUrl = shop.logoUrl ?: ""
                )
            }
        }
    }
}
```

### ✅ **Chỉnh sửa thông tin**
User có thể chỉnh sửa:
- Tên cửa hàng
- Mô tả
- Địa chỉ
- Số điện thoại
- Giờ mở/đóng cửa
- Phí ship
- Đơn tối thiểu
- Ảnh bìa (optional)
- Logo (optional)

### ✅ **Validation**
Tất cả fields được validate theo rules của backend:
- Tên shop: 3-100 ký tự
- Mô tả: không quá 500 ký tự
- Địa chỉ: không quá 200 ký tự
- SĐT: đúng 10 chữ số
- Giờ: format HH:mm
- Phí ship: tối thiểu 3,000đ
- Đơn tối thiểu: tối thiểu 10,000đ

### ✅ **Lưu thay đổi**
Khi click "Lưu thay đổi":
1. Validate form
2. Gọi `PUT /owner/shop` với multipart/form-data
3. Chỉ gửi fields đã thay đổi
4. Upload ảnh mới nếu user chọn
5. Reload dữ liệu sau khi save thành công
6. Hiển thị success message

**Code trong ViewModel:**
```kotlin
fun updateShop(onSuccess: () -> Unit) {
    if (!validateForm()) return
    
    viewModelScope.launch {
        _uiState.update { it.copy(isSaving = true) }
        
        val result = repository.updateShopWithImages(
            name = state.shopName,
            description = state.description,
            address = state.address,
            phone = state.phone,
            openTime = state.openTime,
            closeTime = state.closeTime,
            shipFeePerOrder = state.shipFee.toInt(),
            minOrderAmount = state.minOrderAmount.toInt(),
            coverImageUri = state.newCoverImageUri,  // Optional
            logoUri = state.newLogoUri                // Optional
        )
        
        result.onSuccess {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    successMessage = "Cập nhật shop thành công!"
                )
            }
            loadShopData()  // Reload để lấy URLs ảnh mới
        }
    }
}
```

---

## 🖼️ UI Features

### **Smart Image Display**
```
┌─────────────────────────────────┐
│  Ảnh bìa              [Hủy]     │ ← Nếu đã chọn ảnh mới
├─────────────────────────────────┤
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │  [Current Image from URL] │  │ ← Ảnh hiện tại
│  │                           │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘

Click để chọn ảnh mới ↓

┌─────────────────────────────────┐
│  Ảnh bìa              [Hủy]     │
├─────────────────────────────────┤
│  ┌───────────────────────────┐  │
│  │                           │  │
│  │  [New Selected Image]     │  │
│  │         [Mới]             │  │ ← Badge
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

**Logic:**
1. Nếu có `newImageUri` → Hiển thị ảnh mới + badge "Mới"
2. Nếu không có ảnh mới nhưng có `currentImageUrl` → Hiển thị ảnh từ backend
3. Nếu không có gì → Hiển thị icon "Chọn ảnh"

---

## 🔄 Luồng hoạt động hoàn chỉnh

```
User vào Settings
    ↓
Click "Thông tin cửa hàng"
    ↓
Navigate to "store_info"
    ↓
ShopManagementScreen được render
    ↓
ViewModel.init() → loadShopData()
    ↓
Repository.getMyShop()
    ↓
GET /owner/shop
    ↓
Backend trả về shop data
    ↓
Update UiState với data
    ↓
UI hiển thị form đã điền sẵn
    ↓
User chỉnh sửa thông tin
    ↓
User click "Lưu thay đổi"
    ↓
ViewModel.updateShop()
    ↓
Validate form
    ↓
Repository.updateShopWithImages()
    ↓
PUT /owner/shop (multipart/form-data)
    ↓
Backend update shop
    ↓
Success → Reload shop data
    ↓
UI hiển thị success message
    ↓
User click back button
    ↓
Navigate back to Settings
```

---

## 📊 API Integration

### **GET /owner/shop**
- **Khi**: Màn hình được mở
- **Response**: Shop object với tất cả thông tin
- **Xử lý**: Fill vào form

### **PUT /owner/shop**
- **Khi**: User click "Lưu thay đổi"
- **Content-Type**: `multipart/form-data`
- **Body**: Tất cả fields (optional) + ảnh (optional)
- **Response**: Success message
- **Xử lý**: Reload data + hiển thị success

---

## 🎨 UI States

### **Loading State**
```kotlin
if (uiState.isLoading) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = PrimaryOrange)
    }
}
```

### **Saving State**
```kotlin
Button(
    onClick = { viewModel.updateShop {} },
    enabled = !uiState.isSaving
) {
    if (uiState.isSaving) {
        CircularProgressIndicator(...)
    } else {
        Text("Lưu thay đổi")
    }
}
```

### **Error State**
```kotlin
if (uiState.errorMessage != null) {
    ErrorCard(message = uiState.errorMessage!!)
}
```

### **Success State**
```kotlin
if (uiState.successMessage != null) {
    SuccessCard(message = uiState.successMessage!!)
}
```

---

## ✅ Kết quả

### **Trước:**
- ❌ Dữ liệu hardcoded
- ❌ Không gọi API
- ❌ Không lưu được thay đổi
- ❌ Không hiển thị ảnh thật

### **Sau:**
- ✅ Load dữ liệu từ backend
- ✅ Hiển thị thông tin shop thật
- ✅ Chỉnh sửa và lưu thay đổi
- ✅ Upload ảnh mới
- ✅ Validation đầy đủ
- ✅ Loading/Error/Success states
- ✅ Back navigation

---

## 🚀 Cách test

1. **Mở app** → Login với tài khoản Owner
2. **Vào Settings** (icon ⚙️ ở sidebar)
3. **Click "Thông tin cửa hàng"**
4. **Kiểm tra**:
   - ✅ Loading indicator hiển thị
   - ✅ Form được fill với dữ liệu từ backend
   - ✅ Ảnh hiện tại được hiển thị
5. **Chỉnh sửa** một số fields
6. **Click "Lưu thay đổi"**
7. **Kiểm tra**:
   - ✅ Saving indicator hiển thị
   - ✅ Success message xuất hiện
   - ✅ Dữ liệu được reload
8. **Click back button**
9. **Kiểm tra**: Quay lại Settings screen

---

## 📁 Files liên quan

- `ShopManagementScreen.kt` - UI màn hình
- `ShopManagementViewModel.kt` - Business logic
- `ShopManagementUiState.kt` - State definition
- `ShopRepository.kt` - API calls
- `ShopApiService.kt` - API endpoints
- `SettingsNavHost.kt` - Navigation config

---

**Hoàn thành:** 2026-01-15  
**Tích hợp API:** ✅ GET /owner/shop, ✅ PUT /owner/shop  
**Status:** Ready for testing 🎉
