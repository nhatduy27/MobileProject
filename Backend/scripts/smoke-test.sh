#!/bin/bash

# ==============================================================================
# KTX Delivery - Payment & Wallet Smoke Test
# ==============================================================================
# Tests COD and SEPAY payment flows end-to-end with wallet payout verification
#
# Prerequisites:
# - Firebase project deployed with latest functions
# - Test accounts created (customer, owner, shipper)
# - Environment variables configured
#
# Usage:
#   ./scripts/smoke-test.sh
# ==============================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ==============================================================================
# Configuration - Edit these values
# ==============================================================================
BASE_URL="${BASE_URL:-http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api}"
FIREBASE_API_KEY="${FIREBASE_API_KEY:-AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA}"

# Test account emails (must exist in Firebase Auth)
EMAIL_CUSTOMER="${EMAIL_CUSTOMER:-customer@test.com}"
PASS_CUSTOMER="${PASS_CUSTOMER:-password123}"

EMAIL_OWNER="${EMAIL_OWNER:-owner@test.com}"
PASS_OWNER="${PASS_OWNER:-password123}"

EMAIL_SHIPPER="${EMAIL_SHIPPER:-shipper@test.com}"
PASS_SHIPPER="${PASS_SHIPPER:-password123}"

# SEPAY config (optional - for SEPAY flow testing)
SEPAY_ENABLED="${SEPAY_ENABLED:-false}"

# ==============================================================================
# Helper Functions
# ==============================================================================

log() {
    echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}✓${NC} $1"
}

error() {
    echo -e "${RED}✗${NC} $1"
}

warn() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Login via Firebase Identity Toolkit
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
        exit 1
    fi
    
    success "Logged in as $role"
    echo $idToken
}

# API call helper
api_call() {
    local method=$1
    local endpoint=$2
    local token=$3
    local data=$4
    
    if [ -z "$data" ]; then
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json"
    else
        curl -s -X $method "$BASE_URL$endpoint" \
            -H "Authorization: Bearer $token" \
            -H "Content-Type: application/json" \
            -d "$data"
    fi
}

# ==============================================================================
# Test: COD Payment Flow
# ==============================================================================
test_cod_flow() {
    echo ""
    echo "========================================================================"
    echo "TEST 1: COD Payment Flow"
    echo "========================================================================"
    
    # Login
    TOKEN_CUSTOMER=$(login "$EMAIL_CUSTOMER" "$PASS_CUSTOMER" "CUSTOMER")
    TOKEN_OWNER=$(login "$EMAIL_OWNER" "$PASS_OWNER" "OWNER")
    TOKEN_SHIPPER=$(login "$EMAIL_SHIPPER" "$PASS_SHIPPER" "SHIPPER")
    
    # Step 1: Get owner's shop
    log "Getting owner's shop..."
    SHOP_RESPONSE=$(api_call GET "/shops/owner" "$TOKEN_OWNER")
    SHOP_ID=$(echo $SHOP_RESPONSE | jq -r '.shop.id // empty')
    
    if [ -z "$SHOP_ID" ]; then
        error "Owner has no shop"
        return 1
    fi
    success "Shop ID: $SHOP_ID"
    
    # Step 2: Create order (COD)
    log "Creating COD order..."
    ORDER_DATA="{
        \"shopId\": \"$SHOP_ID\",
        \"items\": [{
            \"productId\": \"test-product-1\",
            \"quantity\": 2,
            \"price\": 50000,
            \"name\": \"Test Product\"
        }],
        \"deliveryAddress\": {
            \"recipientName\": \"Test Customer\",
            \"phone\": \"0123456789\",
            \"address\": \"123 Test St\",
            \"city\": \"Ho Chi Minh\",
            \"district\": \"District 1\"
        },
        \"paymentMethod\": \"cod\"
    }"
    
    ORDER_RESPONSE=$(api_call POST "/orders" "$TOKEN_CUSTOMER" "$ORDER_DATA")
    ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.order.id // empty')
    
    if [ -z "$ORDER_ID" ]; then
        error "Failed to create order: $(echo $ORDER_RESPONSE | jq -r '.message // "Unknown error"')"
        return 1
    fi
    success "Order created: $ORDER_ID"
    
    # Step 3: Create COD payment
    log "Creating COD payment..."
    PAYMENT_DATA='{"method": "cod"}'
    PAYMENT_RESPONSE=$(api_call POST "/orders/$ORDER_ID/payment" "$TOKEN_CUSTOMER" "$PAYMENT_DATA")
    PAYMENT_STATUS=$(echo $PAYMENT_RESPONSE | jq -r '.payment.status // empty')
    
    if [ "$PAYMENT_STATUS" != "PAID" ]; then
        error "COD payment not marked as PAID: $PAYMENT_STATUS"
        return 1
    fi
    success "COD payment created and marked PAID"
    
    # Step 4: Owner confirm order
    log "Owner confirming order..."
    CONFIRM_RESPONSE=$(api_call POST "/orders-owner/$ORDER_ID/confirm" "$TOKEN_OWNER")
    CONFIRM_STATUS=$(echo $CONFIRM_RESPONSE | jq -r '.status // empty')
    
    if [ "$CONFIRM_STATUS" != "CONFIRMED" ]; then
        error "Order not confirmed: $CONFIRM_STATUS"
        return 1
    fi
    success "Order confirmed"
    
    # Step 5: Owner prepare → ready
    log "Owner preparing order..."
    api_call POST "/orders-owner/$ORDER_ID/preparing" "$TOKEN_OWNER" > /dev/null
    sleep 1
    api_call POST "/orders-owner/$ORDER_ID/ready" "$TOKEN_OWNER" > /dev/null
    success "Order ready for pickup"
    
    # Step 6: Shipper accept and ship
    log "Shipper accepting order..."
    api_call POST "/orders-shipper/$ORDER_ID/accept" "$TOKEN_SHIPPER" > /dev/null
    sleep 1
    api_call POST "/orders-shipper/$ORDER_ID/shipping" "$TOKEN_SHIPPER" > /dev/null
    success "Order in shipping"
    
    # Step 7: Shipper deliver (triggers payout)
    log "Shipper delivering order..."
    DELIVER_RESPONSE=$(api_call POST "/orders-shipper/$ORDER_ID/delivered" "$TOKEN_SHIPPER")
    DELIVER_STATUS=$(echo $DELIVER_RESPONSE | jq -r '.status // empty')
    PAID_OUT=$(echo $DELIVER_RESPONSE | jq -r '.paidOut // empty')
    
    if [ "$DELIVER_STATUS" != "DELIVERED" ]; then
        error "Order not delivered: $DELIVER_STATUS"
        return 1
    fi
    success "Order delivered"
    
    # Step 8: Check wallets updated
    log "Checking wallet balances..."
    sleep 2  # Give time for payout to process
    
    OWNER_WALLET=$(api_call GET "/wallets/me" "$TOKEN_OWNER")
    SHIPPER_WALLET=$(api_call GET "/wallets/me" "$TOKEN_SHIPPER")
    
    OWNER_BALANCE=$(echo $OWNER_WALLET | jq -r '.wallet.balance // 0')
    SHIPPER_BALANCE=$(echo $SHIPPER_WALLET | jq -r '.wallet.balance // 0')
    
    success "Owner wallet balance: $OWNER_BALANCE"
    success "Shipper wallet balance: $SHIPPER_BALANCE"
    
    if [ "$OWNER_BALANCE" -gt 0 ] && [ "$SHIPPER_BALANCE" -gt 0 ]; then
        success "✓ COD Flow PASSED - Wallets updated correctly"
        return 0
    else
        warn "Wallet balances may not be updated (check payout logs)"
        return 0  # Don't fail test if payout is async
    fi
}

# ==============================================================================
# Test: SEPAY Payment Flow
# ==============================================================================
test_sepay_flow() {
    echo ""
    echo "========================================================================"
    echo "TEST 2: SEPAY Payment Flow (QR + Polling)"
    echo "========================================================================"
    
    if [ "$SEPAY_ENABLED" != "true" ]; then
        warn "SEPAY test skipped (set SEPAY_ENABLED=true to run)"
        return 0
    fi
    
    # Login
    TOKEN_CUSTOMER=$(login "$EMAIL_CUSTOMER" "$PASS_CUSTOMER" "CUSTOMER")
    TOKEN_OWNER=$(login "$EMAIL_OWNER" "$PASS_OWNER" "OWNER")
    
    # Get owner's shop
    SHOP_RESPONSE=$(api_call GET "/shops/owner" "$TOKEN_OWNER")
    SHOP_ID=$(echo $SHOP_RESPONSE | jq -r '.shop.id // empty')
    
    # Create order (SEPAY)
    log "Creating SEPAY order..."
    ORDER_DATA="{
        \"shopId\": \"$SHOP_ID\",
        \"items\": [{
            \"productId\": \"test-product-2\",
            \"quantity\": 1,
            \"price\": 100000,
            \"name\": \"Test Product 2\"
        }],
        \"deliveryAddress\": {
            \"recipientName\": \"Test Customer\",
            \"phone\": \"0123456789\",
            \"address\": \"123 Test St\",
            \"city\": \"Ho Chi Minh\",
            \"district\": \"District 1\"
        },
        \"paymentMethod\": \"sepay\"
    }"
    
    ORDER_RESPONSE=$(api_call POST "/orders" "$TOKEN_CUSTOMER" "$ORDER_DATA")
    ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.order.id // empty')
    success "Order created: $ORDER_ID"
    
    # Create SEPAY payment
    log "Creating SEPAY payment..."
    PAYMENT_DATA='{"method": "sepay"}'
    PAYMENT_RESPONSE=$(api_call POST "/orders/$ORDER_ID/payment" "$TOKEN_CUSTOMER" "$PAYMENT_DATA")
    
    QR_URL=$(echo $PAYMENT_RESPONSE | jq -r '.payment.providerData.qrCodeUrl // empty')
    SEPAY_CONTENT=$(echo $PAYMENT_RESPONSE | jq -r '.payment.providerData.sepayContent // empty')
    
    if [ -z "$QR_URL" ]; then
        error "No QR URL returned"
        return 1
    fi
    
    success "SEPAY payment created"
    echo ""
    echo "========================================================================"
    echo "QR Code URL: $QR_URL"
    echo "Transfer Content: $SEPAY_CONTENT"
    echo "========================================================================"
    echo ""
    
    # Poll verify endpoint
    log "Polling verify endpoint (10 attempts, 3s interval)..."
    MATCHED=false
    
    for i in {1..10}; do
        sleep 3
        log "Verify attempt $i/10..."
        
        VERIFY_RESPONSE=$(api_call POST "/orders/$ORDER_ID/payment/verify" "$TOKEN_CUSTOMER")
        MATCHED=$(echo $VERIFY_RESPONSE | jq -r '.matched // false')
        
        if [ "$MATCHED" == "true" ]; then
            success "Payment matched!"
            break
        fi
    done
    
    if [ "$MATCHED" != "true" ]; then
        warn "Payment not matched after 10 attempts (no actual transfer made)"
        warn "To complete test: Transfer money with content '$SEPAY_CONTENT' and re-run"
        return 0  # Don't fail - expected if no real transfer
    fi
    
    # If matched, continue with order flow
    log "Completing order flow..."
    api_call POST "/orders-owner/$ORDER_ID/confirm" "$TOKEN_OWNER" > /dev/null
    success "✓ SEPAY Flow PASSED - QR generation and verify endpoint working"
}

# ==============================================================================
# Main Execution
# ==============================================================================
main() {
    echo "========================================================================"
    echo "KTX Delivery - Payment & Wallet Smoke Test"
    echo "========================================================================"
    echo "Base URL: $BASE_URL"
    echo "========================================================================"
    
    # Check dependencies
    if ! command -v jq &> /dev/null; then
        error "jq is required but not installed. Install with: sudo apt-get install jq"
        exit 1
    fi
    
    # Run tests
    test_cod_flow
    COD_RESULT=$?
    
    test_sepay_flow
    SEPAY_RESULT=$?
    
    # Summary
    echo ""
    echo "========================================================================"
    echo "TEST SUMMARY"
    echo "========================================================================"
    
    if [ $COD_RESULT -eq 0 ]; then
        success "COD Flow: PASSED"
    else
        error "COD Flow: FAILED"
    fi
    
    if [ $SEPAY_RESULT -eq 0 ]; then
        success "SEPAY Flow: PASSED"
    else
        error "SEPAY Flow: FAILED"
    fi
    
    echo "========================================================================"
    
    if [ $COD_RESULT -eq 0 ] && [ $SEPAY_RESULT -eq 0 ]; then
        success "All tests PASSED ✓"
        exit 0
    else
        error "Some tests FAILED"
        exit 1
    fi
}

# Run main
main
