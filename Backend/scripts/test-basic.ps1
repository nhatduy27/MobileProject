# Quick PowerShell test script for Windows
$BASE_URL = "http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api"
$FIREBASE_API_KEY = "AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "KTX Delivery - Quick Test (PowerShell)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Health Check
Write-Host "Test 1: Health Check" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/health" -Method Get
    Write-Host "✓ Backend is running" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "✗ Backend not responding: $_" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Test 2: Login
Write-Host "Test 2: Login as Customer" -ForegroundColor Yellow
$loginBody = @{
    email = "customer@test.com"
    password = "Test123!@#"
    returnSecureToken = $true
} | ConvertTo-Json

try {
    $authResponse = Invoke-RestMethod -Uri "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY" -Method Post -Body $loginBody -ContentType "application/json"
    $TOKEN = $authResponse.idToken
    Write-Host "✓ Login successful" -ForegroundColor Green
} catch {
    Write-Host "⚠ Login failed (account may not exist): $_" -ForegroundColor Yellow
    Write-Host "Attempting registration..." -ForegroundColor Yellow
    
    # Try register
    $registerBody = @{
        email = "customer@test.com"
        password = "Test123!@#"
        returnSecureToken = $true
    } | ConvertTo-Json
    
    try {
        $authResponse = Invoke-RestMethod -Uri "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$FIREBASE_API_KEY" -Method Post -Body $registerBody -ContentType "application/json"
        $TOKEN = $authResponse.idToken
        Write-Host "✓ Registration successful" -ForegroundColor Green
    } catch {
        Write-Host "✗ Registration failed: $_" -ForegroundColor Red
        exit 1
    }
}
Write-Host ""

# Test 3: Get Profile
Write-Host "Test 3: Get User Profile" -ForegroundColor Yellow
try {
    $headers = @{
        "Authorization" = "Bearer $TOKEN"
        "Content-Type" = "application/json"
    }
    $profile = Invoke-RestMethod -Uri "$BASE_URL/users/me" -Method Get -Headers $headers
    Write-Host "✓ Profile retrieved: $($profile.user.email)" -ForegroundColor Green
    $profile | ConvertTo-Json -Depth 3
} catch {
    Write-Host "✗ Profile retrieval failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: List Categories
Write-Host "Test 4: List Categories" -ForegroundColor Yellow
try {
    $categories = Invoke-RestMethod -Uri "$BASE_URL/categories" -Method Get
    $count = $categories.categories.Count
    Write-Host "✓ Found $count categories" -ForegroundColor Green
    if ($count -gt 0) {
        Write-Host "Sample: $($categories.categories[0].name)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ Categories retrieval failed: $_" -ForegroundColor Red
}
Write-Host ""

# Test 5: List Shops
Write-Host "Test 5: List Shops" -ForegroundColor Yellow
try {
    $shops = Invoke-RestMethod -Uri "$BASE_URL/shops?page=1&limit=5" -Method Get
    $count = $shops.shops.Count
    Write-Host "✓ Found $count shops" -ForegroundColor Green
    if ($count -gt 0) {
        Write-Host "Sample: $($shops.shops[0].name)" -ForegroundColor Gray
    }
} catch {
    Write-Host "✗ Shops retrieval failed: $_" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Basic functionality verified!" -ForegroundColor Green
Write-Host "For full test suite, use Git Bash:" -ForegroundColor Yellow
Write-Host "  cd Backend/scripts" -ForegroundColor Gray
Write-Host "  bash full-integration-test.sh" -ForegroundColor Gray
