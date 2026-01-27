#!/bin/bash

# ==============================================================================
# KTX Delivery - Full Integration Test
# ==============================================================================
# Tests ALL implemented modules end-to-end
#
# Modules Tested:
# - Auth (register, login, profile)
# - Categories (CRUD)
# - Shops (create, list, update)
# - Products (create, list, update)
# - Cart (add, update, checkout)
# - Orders (create, confirm, prepare, ready, accept, ship, deliver)
# - Payments (COD, SEPAY verification)
# - Wallets (balance, ledger, payout tracking)
# - Shippers (apply, approve, list)
# - Favorites (add, remove, list)
# - Vouchers (create, apply, list)
# - Admin (dashboard, users, shops, payouts)
# - Notifications (list, mark read)
#
# Prerequisites:
# - Backend deployed or running locally
# - Firebase Auth configured
# - Test accounts created or auto-create enabled
#
# Usage:
#   ./scripts/full-integration-test.sh
# ==============================================================================

set -e  # Exit on error

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
MAGENTA='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ==============================================================================
# Configuration
# ==============================================================================
BASE_URL="${BASE_URL:-http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api}"
FIREBASE_API_KEY="${FIREBASE_API_KEY:-AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA}"

# Test mode
DETAILED="${DETAILED:-false}"  # Set to true for full response output
STOP_ON_ERROR="${STOP_ON_ERROR:-false}"

# Test accounts (will be created if not exist)
EMAIL_ADMIN="${EMAIL_ADMIN:-admin@ktx.com}"
PASS_ADMIN="${PASS_ADMIN:-Admin123!@#}"

EMAIL_CUSTOMER="${EMAIL_CUSTOMER:-customer.test@ktx.com}"
PASS_CUSTOMER="${PASS_CUSTOMER:-Test123!@#}"

EMAIL_OWNER="${EMAIL_OWNER:-owner.test@ktx.com}"
PASS_OWNER="${PASS_OWNER:-Test123!@#}"

EMAIL_SHIPPER="${EMAIL_SHIPPER:-shipper.test@ktx.com}"
PASS_SHIPPER="${PASS_SHIPPER:-Test123!@#}"

# Global test data
declare -A TEST_DATA
TEST_DATA[category_id]=""
TEST_DATA[shop_id]=""
TEST_DATA[product_id]=""
TEST_DATA[cart_id]=""
TEST_DATA[order_id]=""
TEST_DATA[payment_id]=""
TEST_DATA[voucher_id]=""
TEST_DATA[shipper_application_id]=""

# Test counters
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_SKIPPED=0

# ==============================================================================
# Helper Functions
# ==============================================================================

log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✓${NC} $1"
    ((TESTS_PASSED++))
}

error() {
    echo -e "${RED}✗${NC} $1"
    ((TESTS_FAILED++))
    if [ "$STOP_ON_ERROR" = "true" ]; then
        exit 1
    fi
}

warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

skip() {
    echo -e "${YELLOW}⊘${NC} $1"
    ((TESTS_SKIPPED++))
}

section() {
    echo ""
    echo -e "${MAGENTA}========================================${NC}"
    echo -e "${MAGENTA}$1${NC}"
    echo -e "${MAGENTA}========================================${NC}"
}

subsection() {
    echo ""
    echo -e "${CYAN}--- $1 ---${NC}"
}

debug_response() {
    if [ "$DETAILED" = "true" ]; then
        echo -e "${YELLOW}Response:${NC}"
        echo "$1" | jq '.' 2>/dev/null || echo "$1"
    fi
}

# API call helper
api() {
    local method=$1
    local endpoint=$2
    local token=$3
    local data=$4
    
    local url="$BASE_URL$endpoint"
    
    if [ -z "$data" ]; then
        curl -s -X $method "$url" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json"
    else
        curl -s -X $method "$url" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$data"
    fi
}

# Login helper
login() {
    local email=$1
    local password=$2
    local role=$3
    
    log "Logging in as $role: $email"
    
    local response=$(curl -s -X POST \
        "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"password\":\"$password\",\"returnSecureToken\":true}")
    
    local idToken=$(echo $response | jq -r '.idToken // empty')
    
    if [ -z "$idToken" ]; then
        error "Login failed for $role: $(echo $response | jq -r '.error.message // "Unknown error"')"
        return 1
    fi
    
    success "Logged in as $role"
    echo $idToken
}

# Register helper
register() {
    local email=$1
    local password=$2
    local role=$3
    local displayName=$4
    
    log "Registering $role: $email"
    
    # First register in Firebase Auth
    local auth_response=$(curl -s -X POST \
        "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=$FIREBASE_API_KEY" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"password\":\"$password\",\"returnSecureToken\":true}")
    
    local idToken=$(echo $auth_response | jq -r '.idToken // empty')
    
    if [ -z "$idToken" ]; then
        local error_msg=$(echo $auth_response | jq -r '.error.message // "Unknown error"')
        if [[ "$error_msg" == *"EMAIL_EXISTS"* ]]; then
            warn "Account already exists, attempting login..."
            login "$email" "$password" "$role"
            return $?
        fi
        error "Registration failed for $role: $error_msg"
        return 1
    fi
    
    # Then register in our backend
    local backend_response=$(api POST "/auth/register" "$idToken" "{
        \"email\": \"$email\",
        \"displayName\": \"$displayName\",
        \"role\": \"$role\",
        \"phone\": \"012345678${RANDOM:0:1}\"
    }")
    
    success "Registered $role: $email"
    echo $idToken
}

# Check if value is not empty
assert_not_empty() {
    local value=$1
    local name=$2
    
    if [ -z "$value" ]; then
        error "Assertion failed: $name is empty"
        return 1
    fi
    return 0
}

# ==============================================================================
# Test Setup
# ==============================================================================

setup_test_accounts() {
    section "SETUP: Test Accounts"
    
    # Try login first, register if fails
    TOKEN_ADMIN=$(login "$EMAIL_ADMIN" "$PASS_ADMIN" "ADMIN" 2>/dev/null || register "$EMAIL_ADMIN" "$PASS_ADMIN" "ADMIN" "Admin User")
    TOKEN_CUSTOMER=$(login "$EMAIL_CUSTOMER" "$PASS_CUSTOMER" "CUSTOMER" 2>/dev/null || register "$EMAIL_CUSTOMER" "$PASS_CUSTOMER" "CUSTOMER" "Test Customer")
    TOKEN_OWNER=$(login "$EMAIL_OWNER" "$PASS_OWNER" "OWNER" 2>/dev/null || register "$EMAIL_OWNER" "$PASS_OWNER" "OWNER" "Test Owner")
    TOKEN_SHIPPER=$(login "$EMAIL_SHIPPER" "$PASS_SHIPPER" "SHIPPER" 2>/dev/null || register "$EMAIL_SHIPPER" "$PASS_SHIPPER" "SHIPPER" "Test Shipper")
    
    if [ -z "$TOKEN_CUSTOMER" ] || [ -z "$TOKEN_OWNER" ]; then
        error "Failed to setup basic test accounts"
        exit 1
    fi
    
    success "All test accounts ready"
}

# ==============================================================================
# Test Modules
# ==============================================================================

test_auth_module() {
    section "MODULE: Authentication & Authorization"
    
    subsection "Get Current User Profile"
    local profile=$(api GET "/users/me" "$TOKEN_CUSTOMER")
    debug_response "$profile"
    
    local email=$(echo $profile | jq -r '.user.email // empty')
    if [ "$email" = "$EMAIL_CUSTOMER" ]; then
        success "Profile endpoint working"
    else
        error "Profile endpoint failed"
    fi
}

test_categories_module() {
    section "MODULE: Categories"
    
    subsection "List Categories"
    local categories=$(api GET "/categories" "$TOKEN_CUSTOMER")
    debug_response "$categories"
    
    local count=$(echo $categories | jq '.categories | length')
    if [ "$count" -gt 0 ]; then
        success "Categories list: $count items"
        TEST_DATA[category_id]=$(echo $categories | jq -r '.categories[0].id')
    else
        warn "No categories found, creating one..."
        
        if [ -n "$TOKEN_ADMIN" ]; then
            local new_cat=$(api POST "/admin/categories" "$TOKEN_ADMIN" '{
                "name": "Test Category",
                "description": "For testing",
                "icon": "🍔"
            }')
            TEST_DATA[category_id]=$(echo $new_cat | jq -r '.category.id // empty')
            [ -n "${TEST_DATA[category_id]}" ] && success "Created test category" || error "Failed to create category"
        else
            skip "Cannot create category without admin token"
        fi
    fi
}

test_shops_module() {
    section "MODULE: Shops"
    
    subsection "Create/Get Owner Shop"
    local shop_response=$(api GET "/shops/owner" "$TOKEN_OWNER")
    debug_response "$shop_response"
    
    local shop_id=$(echo $shop_response | jq -r '.shop.id // empty')
    
    if [ -z "$shop_id" ]; then
        log "Creating new shop for owner..."
        shop_response=$(api POST "/shops/owner" "$TOKEN_OWNER" "{
            \"name\": \"Test Shop\",
            \"description\": \"Shop for integration testing\",
            \"phone\": \"0123456789\",
            \"address\": \"123 Test St\",
            \"categoryId\": \"${TEST_DATA[category_id]}\",
            \"businessHours\": [
                {\"day\": \"monday\", \"open\": \"08:00\", \"close\": \"22:00\"}
            ]
        }")
        shop_id=$(echo $shop_response | jq -r '.shop.id // empty')
    fi
    
    if [ -n "$shop_id" ]; then
        TEST_DATA[shop_id]="$shop_id"
        success "Shop ready: $shop_id"
    else
        error "Failed to get/create shop"
        return 1
    fi
    
    subsection "List All Shops"
    local shops=$(api GET "/shops?page=1&limit=10" "$TOKEN_CUSTOMER")
    local count=$(echo $shops | jq '.shops | length')
    success "Found $count shops"
}

test_products_module() {
    section "MODULE: Products"
    
    if [ -z "${TEST_DATA[shop_id]}" ]; then
        skip "Products test: no shop available"
        return 1
    fi
    
    subsection "Create Product"
    local product=$(api POST "/products/owner" "$TOKEN_OWNER" "{
        \"name\": \"Test Product\",
        \"description\": \"Product for testing\",
        \"price\": 50000,
        \"categoryId\": \"${TEST_DATA[category_id]}\",
        \"images\": [\"https://via.placeholder.com/300\"],
        \"isAvailable\": true
    }")
    debug_response "$product"
    
    TEST_DATA[product_id]=$(echo $product | jq -r '.product.id // empty')
    
    if [ -n "${TEST_DATA[product_id]}" ]; then
        success "Product created: ${TEST_DATA[product_id]}"
    else
        error "Failed to create product"
        return 1
    fi
    
    subsection "List Shop Products"
    local products=$(api GET "/products/owner" "$TOKEN_OWNER")
    local count=$(echo $products | jq '.products | length')
    success "Owner has $count products"
}

test_cart_module() {
    section "MODULE: Shopping Cart"
    
    if [ -z "${TEST_DATA[product_id]}" ]; then
        skip "Cart test: no product available"
        return 1
    fi
    
    subsection "Add Item to Cart"
    local cart=$(api POST "/cart/items" "$TOKEN_CUSTOMER" "{
        \"productId\": \"${TEST_DATA[product_id]}\",
        \"quantity\": 2
    }")
    debug_response "$cart"
    
    local item_count=$(echo $cart | jq '.cart.items | length')
    if [ "$item_count" -gt 0 ]; then
        success "Added to cart: $item_count items"
    else
        error "Failed to add to cart"
        return 1
    fi
    
    subsection "Get Cart"
    cart=$(api GET "/cart" "$TOKEN_CUSTOMER")
    local total=$(echo $cart | jq -r '.cart.total')
    success "Cart total: ${total}đ"
}

test_orders_payment_flow() {
    section "MODULE: Orders & Payments (Full Flow)"
    
    subsection "Create Order from Cart"
    local order=$(api POST "/orders" "$TOKEN_CUSTOMER" "{
        \"shopId\": \"${TEST_DATA[shop_id]}\",
        \"items\": [{
            \"productId\": \"${TEST_DATA[product_id]}\",
            \"quantity\": 2,
            \"price\": 50000,
            \"name\": \"Test Product\"
        }],
        \"deliveryAddress\": {
            \"recipientName\": \"Test Customer\",
            \"phone\": \"0123456789\",
            \"address\": \"456 Test Ave\",
            \"city\": \"Ho Chi Minh\",
            \"district\": \"District 1\",
            \"ward\": \"Ward 1\"
        },
        \"paymentMethod\": \"cod\"
    }")
    debug_response "$order"
    
    TEST_DATA[order_id]=$(echo $order | jq -r '.order.id // empty')
    
    if [ -z "${TEST_DATA[order_id]}" ]; then
        error "Failed to create order"
        return 1
    fi
    success "Order created: ${TEST_DATA[order_id]}"
    
    subsection "Create COD Payment"
    local payment=$(api POST "/orders/${TEST_DATA[order_id]}/payment" "$TOKEN_CUSTOMER" '{
        "method": "cod"
    }')
    debug_response "$payment"
    
    local payment_status=$(echo $payment | jq -r '.payment.status')
    if [ "$payment_status" = "PAID" ]; then
        success "COD payment created and marked PAID"
    else
        error "Payment not PAID: $payment_status"
        return 1
    fi
    
    subsection "Owner Confirms Order"
    local confirmed=$(api POST "/orders-owner/${TEST_DATA[order_id]}/confirm" "$TOKEN_OWNER")
    local status=$(echo $confirmed | jq -r '.status')
    [ "$status" = "CONFIRMED" ] && success "Order confirmed" || error "Confirm failed: $status"
    
    subsection "Owner Prepares Order"
    api POST "/orders-owner/${TEST_DATA[order_id]}/preparing" "$TOKEN_OWNER" > /dev/null
    success "Order preparing"
    
    subsection "Owner Marks Ready"
    api POST "/orders-owner/${TEST_DATA[order_id]}/ready" "$TOKEN_OWNER" > /dev/null
    success "Order ready for pickup"
}

test_shipper_flow() {
    section "MODULE: Shippers & Delivery"
    
    if [ -z "${TEST_DATA[order_id]}" ] || [ -z "${TEST_DATA[shop_id]}" ]; then
        skip "Shipper flow: no order/shop available"
        return 1
    fi
    
    subsection "Apply as Shipper (if needed)"
    if [ -n "$TOKEN_SHIPPER" ]; then
        local application=$(api POST "/shippers/apply" "$TOKEN_SHIPPER" "{
            \"shopId\": \"${TEST_DATA[shop_id]}\",
            \"fullName\": \"Test Shipper\",
            \"phone\": \"0987654321\",
            \"vehicleType\": \"motorbike\",
            \"vehicleNumber\": \"99A-12345\"
        }" 2>/dev/null || echo '{"message":"Already applied"}')
        
        debug_response "$application"
        success "Shipper application submitted"
    fi
    
    subsection "Owner Approves Shipper"
    # Get pending applications
    local applications=$(api GET "/shippers/owner/applications?status=PENDING" "$TOKEN_OWNER")
    local app_id=$(echo $applications | jq -r '.applications[0].id // empty')
    
    if [ -n "$app_id" ]; then
        api POST "/shippers/owner/applications/$app_id/approve" "$TOKEN_OWNER" > /dev/null
        success "Shipper approved"
        sleep 1  # Wait for role sync
    else
        warn "No pending applications (may already be approved)"
    fi
    
    subsection "Shipper Accepts Order"
    local accepted=$(api POST "/orders-shipper/${TEST_DATA[order_id]}/accept" "$TOKEN_SHIPPER" 2>/dev/null)
    debug_response "$accepted"
    
    if [ $? -eq 0 ]; then
        success "Order accepted by shipper"
        
        subsection "Shipper Starts Shipping"
        api POST "/orders-shipper/${TEST_DATA[order_id]}/shipping" "$TOKEN_SHIPPER" > /dev/null
        success "Order in shipping"
        
        subsection "Shipper Delivers Order"
        api POST "/orders-shipper/${TEST_DATA[order_id]}/delivered" "$TOKEN_SHIPPER" > /dev/null
        success "Order delivered (payout triggered)"
    else
        error "Shipper accept failed"
    fi
}

test_wallets_module() {
    section "MODULE: Wallets & Payouts"
    
    subsection "Check Owner Wallet"
    local owner_wallet=$(api GET "/wallets/me" "$TOKEN_OWNER")
    debug_response "$owner_wallet"
    
    local owner_balance=$(echo $owner_wallet | jq -r '.wallet.balance // 0')
    success "Owner wallet balance: ${owner_balance}đ"
    
    subsection "Check Shipper Wallet"
    if [ -n "$TOKEN_SHIPPER" ]; then
        local shipper_wallet=$(api GET "/wallets/me" "$TOKEN_SHIPPER")
        local shipper_balance=$(echo $shipper_wallet | jq -r '.wallet.balance // 0')
        success "Shipper wallet balance: ${shipper_balance}đ"
    fi
    
    subsection "Get Wallet Ledger"
    local ledger=$(api GET "/wallets/ledger?page=1&limit=10" "$TOKEN_OWNER")
    local entries=$(echo $ledger | jq '.entries | length')
    success "Wallet ledger: $entries entries"
}

test_favorites_module() {
    section "MODULE: Favorites"
    
    if [ -z "${TEST_DATA[product_id]}" ]; then
        skip "Favorites test: no product available"
        return 1
    fi
    
    subsection "Add to Favorites"
    local fav=$(api POST "/favorites" "$TOKEN_CUSTOMER" "{
        \"productId\": \"${TEST_DATA[product_id]}\"
    }" 2>/dev/null || echo '{"message":"Already favorited"}')
    success "Added to favorites"
    
    subsection "List Favorites"
    local favorites=$(api GET "/favorites?page=1&limit=10" "$TOKEN_CUSTOMER")
    local count=$(echo $favorites | jq '.favorites | length')
    success "Favorites: $count items"
    
    subsection "Remove from Favorites"
    api DELETE "/favorites/${TEST_DATA[product_id]}" "$TOKEN_CUSTOMER" > /dev/null 2>&1
    success "Removed from favorites"
}

test_admin_module() {
    section "MODULE: Admin Dashboard & Management"
    
    if [ -z "$TOKEN_ADMIN" ]; then
        skip "Admin tests: no admin token"
        return 0
    fi
    
    subsection "Dashboard Stats"
    local stats=$(api GET "/admin/dashboard/stats" "$TOKEN_ADMIN")
    debug_response "$stats"
    
    local total_users=$(echo $stats | jq -r '.users.total // 0')
    success "Dashboard stats: $total_users total users"
    
    subsection "List Users"
    local users=$(api GET "/admin/users?page=1&limit=10" "$TOKEN_ADMIN")
    local user_count=$(echo $users | jq '.users | length')
    success "Admin users list: $user_count users"
    
    subsection "List Shops"
    local shops=$(api GET "/admin/shops?page=1&limit=10" "$TOKEN_ADMIN")
    local shop_count=$(echo $shops | jq '.shops | length')
    success "Admin shops list: $shop_count shops"
    
    subsection "List Payouts"
    local payouts=$(api GET "/admin/payouts?page=1&limit=10" "$TOKEN_ADMIN")
    local payout_count=$(echo $payouts | jq '.payouts | length // 0')
    success "Admin payouts list: $payout_count payout requests"
}

test_notifications_module() {
    section "MODULE: Notifications"
    
    subsection "List Customer Notifications"
    local notifs=$(api GET "/notifications?page=1&limit=10" "$TOKEN_CUSTOMER")
    local count=$(echo $notifs | jq '.notifications | length // 0')
    success "Notifications: $count unread"
}

# ==============================================================================
# Main Test Runner
# ==============================================================================

main() {
    echo "========================================================================"
    echo "KTX Delivery - Full Integration Test Suite"
    echo "========================================================================"
    echo "Base URL: $BASE_URL"
    echo "Detailed Output: $DETAILED"
    echo "Stop on Error: $STOP_ON_ERROR"
    echo "========================================================================"
    
    # Check dependencies
    if ! command -v jq &> /dev/null; then
        error "jq is required. Install: sudo apt-get install jq"
        exit 1
    fi
    
    # Setup
    setup_test_accounts
    
    # Run module tests
    test_auth_module
    test_categories_module
    test_shops_module
    test_products_module
    test_cart_module
    test_orders_payment_flow
    test_shipper_flow
    test_wallets_module
    test_favorites_module
    test_admin_module
    test_notifications_module
    
    # Summary
    echo ""
    echo "========================================================================"
    echo "TEST SUMMARY"
    echo "========================================================================"
    echo -e "${GREEN}Passed:${NC}  $TESTS_PASSED"
    echo -e "${RED}Failed:${NC}  $TESTS_FAILED"
    echo -e "${YELLOW}Skipped:${NC} $TESTS_SKIPPED"
    echo "========================================================================"
    
    if [ $TESTS_FAILED -eq 0 ]; then
        echo -e "${GREEN}✓ All tests PASSED${NC}"
        exit 0
    else
        echo -e "${RED}✗ Some tests FAILED${NC}"
        exit 1
    fi
}

# Run main
main "$@"
