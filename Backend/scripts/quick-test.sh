#!/bin/bash

# Quick test helper - test specific module only

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="${BASE_URL:-http://127.0.0.1:5001/foodappproject-7c136/asia-southeast1/api}"
FIREBASE_API_KEY="${FIREBASE_API_KEY:-AIzaSyDbh9zQqMUuPEvELoWOP6Uukl04qIuTWeA}"

log() { echo -e "${BLUE}[$(date +'%H:%M:%S')]${NC} $1"; }
success() { echo -e "${GREEN}✓${NC} $1"; }
error() { echo -e "${RED}✗${NC} $1"; }

# Login helper
login() {
    local email=$1
    local password=$2
    
    local response=$(curl -s -X POST \
        "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$FIREBASE_API_KEY" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$email\",\"password\":\"$password\",\"returnSecureToken\":true}")
    
    echo $response | jq -r '.idToken // empty'
}

# API helper
api() {
    curl -s -X $1 "$BASE_URL$2" \
        -H "Authorization: Bearer $3" \
        -H "Content-Type: application/json" \
        ${4:+-d "$4"}
}

MODULE=$1

case $MODULE in
    "categories")
        log "Testing Categories..."
        TOKEN=$(login "customer@test.com" "Test123!@#")
        RESULT=$(api GET "/categories" "$TOKEN")
        echo $RESULT | jq '.categories | length' > /dev/null && success "Categories OK" || error "Failed"
        echo $RESULT | jq '.'
        ;;
        
    "shops")
        log "Testing Shops..."
        TOKEN=$(login "owner@test.com" "Test123!@#")
        RESULT=$(api GET "/shops/owner" "$TOKEN")
        echo $RESULT | jq '.shop.id' > /dev/null && success "Shop OK" || error "Failed"
        echo $RESULT | jq '.'
        ;;
        
    "products")
        log "Testing Products..."
        TOKEN=$(login "owner@test.com" "Test123!@#")
        RESULT=$(api GET "/products/owner" "$TOKEN")
        echo $RESULT | jq '.products' > /dev/null && success "Products OK" || error "Failed"
        echo $RESULT | jq '.'
        ;;
        
    "cart")
        log "Testing Cart..."
        TOKEN=$(login "customer@test.com" "Test123!@#")
        RESULT=$(api GET "/cart" "$TOKEN")
        echo $RESULT | jq '.cart' > /dev/null && success "Cart OK" || error "Failed"
        echo $RESULT | jq '.'
        ;;
        
    "orders")
        log "Testing Orders..."
        TOKEN=$(login "customer@test.com" "Test123!@#")
        RESULT=$(api GET "/orders?page=1&limit=5" "$TOKEN")
        echo $RESULT | jq '.orders' > /dev/null && success "Orders OK" || error "Failed"
        echo $RESULT | jq '.'
        ;;
        
    "wallets")
        log "Testing Wallets..."
        TOKEN=$(login "owner@test.com" "Test123!@#")
        RESULT=$(api GET "/wallets/me" "$TOKEN")
        echo $RESULT | jq '.wallet.balance' > /dev/null && success "Wallet OK" || error "Failed"
        echo $RESULT | jq '.'
        ;;
        
    "admin")
        log "Testing Admin..."
        TOKEN=$(login "admin@ktx.com" "Admin123!@#")
        RESULT=$(api GET "/admin/dashboard/stats" "$TOKEN")
        echo $RESULT | jq '.users.total' > /dev/null && success "Admin OK" || error "Failed"
        echo $RESULT | jq '.'
        ;;
        
    "health")
        log "Testing Health..."
        curl -s "$BASE_URL/health" | jq '.'
        ;;
        
    *)
        echo "Usage: ./quick-test.sh <module>"
        echo ""
        echo "Available modules:"
        echo "  categories  - Test categories listing"
        echo "  shops       - Test shop endpoints"
        echo "  products    - Test product endpoints"
        echo "  cart        - Test shopping cart"
        echo "  orders      - Test order listing"
        echo "  wallets     - Test wallet balance"
        echo "  admin       - Test admin dashboard"
        echo "  health      - Test backend health"
        echo ""
        echo "Examples:"
        echo "  ./quick-test.sh categories"
        echo "  ./quick-test.sh wallets"
        echo "  BASE_URL=https://... ./quick-test.sh admin"
        exit 1
        ;;
esac
