# GPS Frontend Integration Guide

**For:** Android/Kotlin Mobile Developers  
**Version:** 1.0  
**Last Updated:** January 29, 2026

---

## 1. Introduction

### What is the GPS Module?

The GPS (Shipper Routing) module helps shippers deliver multiple orders efficiently by calculating an optimized route. Instead of manually deciding which orders to deliver in what sequence, the system:

- Takes a list of orders (1-15 orders)
- Calculates the best route using Google Routes API
- Provides turn-by-turn waypoints with optimized visiting order
- Tracks delivery status throughout the trip

### What Problem Does It Solve?

**Without GPS Module:**
- Shipper manually picks orders one by one
- No route optimization → wasted time and fuel
- No clear sequence → confusion about which building to visit first
- Hard to track progress

**With GPS Module:**
- System calculates optimal route automatically
- Clear sequence: Stop 1 → Stop 2 → Stop 3
- Visual map with polyline showing the route
- Backend tracks trip progress (PENDING → STARTED → FINISHED)

### High-Level Lifecycle

```
READY Orders
    ↓
Shipper selects 1-15 orders
    ↓
Create Trip (status: PENDING)
    ↓
Start Trip (status: STARTED, orders → SHIPPING)
    ↓
Deliver orders
    ↓
Finish Trip (status: FINISHED, orders → DELIVERED)
```

---

## 2. Key Concepts

### Trip

A Trip represents one delivery session with multiple orders. It contains:

- **Route information:** Distance, duration, polyline for map rendering
- **Waypoints:** Ordered list of stops (buildings to visit)
- **Orders:** List of orders included in this trip
- **Status:** PENDING → STARTED → FINISHED (or CANCELLED)

Think of a Trip as a "mission" that groups multiple orders together with an optimized route.

### Order vs TripOrder

**Order (from Orders API):**
- Full order details: items, customer, price, etc.
- Has status: PENDING, READY, SHIPPING, DELIVERED, etc.
- Lives in the Orders collection

**TripOrder (inside Trip):**
- Lightweight reference to an order
- Contains: `orderId`, `buildingCode`, `stopIndex`, `tripDeliveryStatus`
- Links orders to specific stops in the route

**Key difference:** Orders API manages order lifecycle. GPS module only tracks which stop each order belongs to.

### Waypoint vs Order

**Waypoint:**
- Represents a physical stop location (a building like "A1", "B2")
- Has GPS coordinates and a visit order (1st stop, 2nd stop, etc.)
- One waypoint can have multiple orders

**Example:**
- Waypoint 1: Building A1 (2 orders)
- Waypoint 2: Building B2 (1 order)
- Waypoint 3: Building C3 (3 orders)

Total: 3 waypoints, 6 orders

**Important:** The number of waypoints ≤ number of orders (when multiple orders go to the same building).

### Trip Status

| Status | Meaning | Frontend Action |
|--------|---------|-----------------|
| `PENDING` | Trip created, not started yet | Show "Start Trip" button |
| `STARTED` | Trip in progress, shipper is delivering | Show "Finish Trip" button |
| `FINISHED` | Trip completed successfully | Show summary, orders delivered |
| `CANCELLED` | Trip cancelled (only from PENDING) | Show cancellation reason |

**State Transitions:**
- PENDING → STARTED (via `start-trip` API)
- STARTED → FINISHED (via `finish-trip` API)
- PENDING → CANCELLED (via `cancel-trip` API)
- ❌ Cannot cancel a STARTED trip
- ❌ Cannot start a FINISHED trip

### tripDeliveryStatus (MVP Limitation)

Each TripOrder has a `tripDeliveryStatus` field:
- `NOT_VISITED`: Order not delivered yet
- `VISITED`: Order delivered
- `FAILED`: Delivery failed (not used in MVP)

**Critical MVP Limitation:**
- `tripDeliveryStatus` updates **ONLY IN BATCH**
- When you call `start-trip`: ALL orders → `NOT_VISITED`
- When you call `finish-trip`: ALL orders → `VISITED`
- No API to mark individual orders as delivered
- No partial delivery support in MVP

**Frontend Impact:** Your UI cannot let shipper mark orders one-by-one during delivery. All orders are marked delivered together when trip finishes.

---

## 3. Typical Shipper Flow

### Step 1: List READY Orders

**Frontend calls:** Orders API (not GPS API)

```kotlin
// Example: Get orders with status READY assigned to current shipper
ordersRepository.getMyOrders(status = "READY")
```

**Filter:** Only show orders that have `buildingCode` (required for GPS).

**UI:** Display list with checkboxes, allow shipper to select 1-15 orders.

---

### Step 2: Select Orders (Max 15)

**Frontend logic:**
```kotlin
val selectedOrders = mutableListOf<String>() // Store order IDs

// Validation before creating trip
if (selectedOrders.size !in 1..15) {
    showError("Please select 1-15 orders")
    return
}

// Check all orders have buildingCode
val hasInvalidOrders = orders.any { it.buildingCode == null }
if (hasInvalidOrders) {
    showError("Some orders missing building code")
    return
}
```

**Important:** Backend enforces max 15 orders due to Google Routes API limits.

---

### Step 3: Create Optimized Trip

**Frontend calls:** `POST /api/gps/create-optimized-trip`

**Request:**
```json
{
  "orderIds": ["order_001", "order_002", "order_003"],
  "origin": {
    "lat": 10.773589,
    "lng": 106.659924,
    "name": "My Location"
  }
}
```

**What happens on backend:**
1. Validates orders (exist, READY status, have buildingCode)
2. Groups orders by buildingCode → creates waypoints
3. Calls Google Routes API to optimize route
4. Creates Trip with status PENDING
5. **Orders remain READY** (not changed yet)

**Response:**
```json
{
  "success": true,
  "data": {
    "id": "trip_abc123",
    "status": "PENDING",
    "waypoints": [
      { "buildingCode": "A1", "location": {...}, "order": 1 },
      { "buildingCode": "B2", "location": {...}, "order": 2 }
    ],
    "orders": [
      { "orderId": "order_001", "buildingCode": "A1", "stopIndex": 1, "tripDeliveryStatus": "NOT_VISITED" },
      { "orderId": "order_002", "buildingCode": "A1", "stopIndex": 1, "tripDeliveryStatus": "NOT_VISITED" },
      { "orderId": "order_003", "buildingCode": "B2", "stopIndex": 2, "tripDeliveryStatus": "NOT_VISITED" }
    ],
    "route": {
      "distance": 2450,
      "duration": 420,
      "polyline": "u~pvFwxdlSabC~aB_fDwqE"
    },
    "totalOrders": 3,
    "totalBuildings": 2
  }
}
```

**Frontend should:**
- Store `tripId` for subsequent API calls
- Navigate to trip detail screen
- Display map with route polyline
- Show waypoints list in order

---

### Step 4: Start Trip

**Frontend calls:** `POST /api/gps/start-trip`

**Request:**
```json
{
  "tripId": "trip_abc123"
}
```

**What happens on backend:**
1. Changes trip status: PENDING → STARTED
2. Sets `startedAt` timestamp
3. **Updates ALL orders:** READY → SHIPPING

**Response:** Updated trip object with `status: "STARTED"` and `startedAt` timestamp.

**Frontend should:**
- Hide "Start Trip" button
- Show "Finish Trip" button
- Optionally: Start tracking shipper location (future feature)

---

### Step 5: Finish Trip

**Frontend calls:** `POST /api/gps/finish-trip`

**Request:**
```json
{
  "tripId": "trip_abc123"
}
```

**What happens on backend:**
1. Changes trip status: STARTED → FINISHED
2. Sets `finishedAt` timestamp
3. **Updates ALL orders:** SHIPPING → DELIVERED
4. Updates all TripOrders: `tripDeliveryStatus` → VISITED

**Response (special format):**
```json
{
  "success": true,
  "data": {
    "trip": { "id": "trip_abc123", "status": "FINISHED", ... },
    "ordersDelivered": 3
  }
}
```

**Note:** Unlike other endpoints, `finish-trip` returns `{ trip, ordersDelivered }` instead of just `trip`.

**Frontend should:**
- Show success message: "3 orders delivered!"
- Navigate to trip history or home screen
- Clear any cached trip data

---

### Step 6: Cancel Trip (Optional)

**Frontend calls:** `POST /api/gps/cancel-trip`

**Only works when trip status is PENDING.** Cannot cancel STARTED trips.

**Request:**
```json
{
  "tripId": "trip_abc123",
  "reason": "Customer requested cancellation"
}
```

**What happens on backend:**
1. Changes trip status: PENDING → CANCELLED
2. Sets `cancelledAt` timestamp and `cancelReason`
3. **Orders remain READY** (not changed)

**Frontend should:**
- Only show "Cancel Trip" button when status is PENDING
- Show confirmation dialog before canceling
- Handle 409 error if user tries to cancel STARTED trip

---

## 4. API Usage Guide

### POST /api/gps/create-optimized-trip

**When to call:** After shipper selects 1-15 READY orders.

**Required input:**
- `orderIds`: Array of order IDs (1-15 items)
- `origin`: Current shipper location (lat, lng)
- `returnTo`: Optional, defaults to origin

**Important response fields:**

| Field | Type | Frontend Usage |
|-------|------|----------------|
| `id` | string | Store for all subsequent API calls |
| `status` | string | Always "PENDING" after creation |
| `waypoints` | array | **Already sorted** by optimal visit order. Display in order. |
| `waypoints[].order` | int | Stop number (1st stop, 2nd stop, ...). Use for UI labels. |
| `orders[].stopIndex` | int | Which stop this order belongs to. Map to waypoint. |
| `route.polyline` | string | Google encoded polyline. Decode and render on map. |
| `route.distance` | int | Total distance in meters. Show to user: "2.4 km route" |
| `route.duration` | int | Estimated time in seconds. Show: "7 minutes" |
| `totalBuildings` | int | Number of unique stops. Can be < totalOrders. |

**What to store locally:**
- `tripId` (required for start/finish/cancel APIs)
- `waypoints` array (for map and list UI)
- `orders` array (to show which orders at each stop)

**Common errors:**
- 400: More than 15 orders, or order missing buildingCode
- 404: Order not found or not assigned to shipper

---

### GET /api/gps/trip

**When to call:** 
- On trip detail screen load
- After start/finish/cancel to refresh data

**Required input:**
- `tripId`: Query parameter

**Response:** Same as create-trip response, but status may be STARTED, FINISHED, or CANCELLED.

**Frontend usage:** Refresh trip data to show latest status and timestamps.

---

### GET /api/gps/trips

**When to call:** 
- Trip history screen
- "My Trips" list

**Optional filters:**
- `status`: PENDING, STARTED, FINISHED, CANCELLED
- `page`: Page number (default 1)
- `limit`: Items per page (default 20, max 50)

**Response:**
```json
{
  "success": true,
  "data": {
    "items": [ /* array of trips */ ],
    "page": 1,
    "limit": 20,
    "total": 42,
    "totalPages": 3,
    "hasNext": true
  }
}
```

**Frontend usage:** 
- Use `hasNext` for "Load More" button
- Show `total` in UI: "Showing 1-20 of 42 trips"

---

### POST /api/gps/start-trip

**When to call:** User taps "Start Trip" button.

**Precondition:** Trip status must be PENDING.

**Required input:** `tripId`

**What changes:**
- Trip: PENDING → STARTED
- Orders: READY → SHIPPING (backend updates Orders collection)

**Frontend action:** Hide "Start Trip" button, show "Finish Trip" button.

**Error handling:** If status is not PENDING, backend returns 409 Conflict.

---

### POST /api/gps/finish-trip

**When to call:** User taps "Finish Trip" button after delivering all orders.

**Precondition:** Trip status must be STARTED.

**Required input:** `tripId`

**What changes:**
- Trip: STARTED → FINISHED
- Orders: SHIPPING → DELIVERED (backend updates Orders collection)
- TripOrders: tripDeliveryStatus → VISITED (all orders marked as delivered)

**Response format (special):**
```json
{
  "success": true,
  "data": {
    "trip": { ... },
    "ordersDelivered": 5
  }
}
```

**Frontend parsing:**
```kotlin
val trip = response.data.trip // NOT response.data directly!
val count = response.data.ordersDelivered
showSuccess("$count orders delivered!")
```

**Error handling:** If status is not STARTED, backend returns 409 Conflict.

---

### POST /api/gps/cancel-trip

**When to call:** User taps "Cancel Trip" (only for PENDING trips).

**Precondition:** Trip status must be PENDING. Cannot cancel STARTED or FINISHED trips.

**Required input:** 
- `tripId`
- `reason`: Optional cancellation reason

**What changes:**
- Trip: PENDING → CANCELLED
- Orders: **Remain READY** (not changed)

**Frontend considerations:**
- Only show "Cancel Trip" button when status is PENDING
- Show confirmation dialog: "Are you sure? This cannot be undone."
- Optional text field for cancellation reason

**Error handling:** If trip is STARTED, backend returns 409 Conflict with message: "Cannot cancel trip. Only PENDING trips can be cancelled."

---

## 5. Maps & Routing Notes

### Waypoints Order vs Orders Array

**Common confusion:** "I sent orders [A, B, C] but waypoints came back [C, A, B]. Is this a bug?"

**Answer:** No. Backend optimizes the route. Input order ≠ output order.

**How to get correct visit sequence:**

```kotlin
// ✅ Correct: Use waypoints array index
trip.waypoints.forEachIndexed { index, waypoint ->
    println("Stop ${index + 1}: ${waypoint.buildingCode}")
}

// ✅ Also correct: Use waypoint.order field
trip.waypoints.forEach { waypoint ->
    println("Stop ${waypoint.order}: ${waypoint.buildingCode}")
}

// ❌ Wrong: Use orders array index
trip.orders.forEach { order ->
    // This order is NOT sorted! Don't use for UI.
}
```

**Key point:** The `waypoints` array is already sorted in visit order. Just display it from index 0 to end.

---

### stopIndex Meaning

Each `TripOrder` has a `stopIndex` field. This tells you which stop (waypoint) the order belongs to.

**Example:**
```json
{
  "waypoints": [
    { "buildingCode": "A1", "order": 1 },
    { "buildingCode": "B2", "order": 2 }
  ],
  "orders": [
    { "orderId": "order_001", "buildingCode": "A1", "stopIndex": 1 },
    { "orderId": "order_002", "buildingCode": "A1", "stopIndex": 1 },
    { "orderId": "order_003", "buildingCode": "B2", "stopIndex": 2 }
  ]
}
```

**Frontend usage:**
```kotlin
// Get all orders for a specific stop
fun getOrdersForStop(trip: Trip, stopIndex: Int): List<TripOrder> {
    return trip.orders.filter { it.stopIndex == stopIndex }
}

// Display in UI
trip.waypoints.forEach { waypoint ->
    val orders = getOrdersForStop(trip, waypoint.order)
    println("Stop ${waypoint.order}: ${waypoint.buildingCode} - ${orders.size} orders")
}
```

Output:
```
Stop 1: A1 - 2 orders
Stop 2: B2 - 1 order
```

---

### Why Multiple Orders Can Share One Building

**Scenario:** Customer A and Customer B both live in building A1.

**Result:** 
- 2 orders
- 1 waypoint (building A1)
- Both orders have `stopIndex: 1`

**Frontend impact:**
- `totalOrders`: 2
- `totalBuildings`: 1
- Display: "1 stop, 2 orders"

**Don't assume:** `totalOrders === waypoints.length`

---

### Polyline Usage for Map Rendering

The `route.polyline` field contains a Google-encoded polyline string.

**Frontend steps:**

1. **Decode polyline:**
```kotlin
// Use Google Maps Utils library
fun decodePolyline(encoded: String): List<LatLng> {
    return PolyUtil.decode(encoded)
}
```

2. **Draw on map:**
```kotlin
val routePoints = decodePolyline(trip.route.polyline)

GoogleMap(...) {
    Polyline(
        points = routePoints,
        color = Color.Blue,
        width = 10f
    )
}
```

3. **Add markers:**
```kotlin
// Origin marker
Marker(
    position = LatLng(trip.origin.lat, trip.origin.lng),
    title = "Start"
)

// Waypoint markers
trip.waypoints.forEach { waypoint ->
    Marker(
        position = LatLng(waypoint.location.lat, waypoint.location.lng),
        title = waypoint.buildingCode,
        snippet = "Stop ${waypoint.order}"
    )
}
```

---

## 6. Limitations (MVP – Important!)

### Static Route Only

- Route is calculated **ONCE** when trip is created
- No live rerouting if shipper takes a different path
- No traffic-aware re-optimization during delivery

**Frontend impact:** Don't show "Recalculate Route" button. Current route is fixed.

---

### No Per-Order Delivery Update

**What you might expect:** API to mark individual orders as delivered.

**MVP reality:** Only batch updates via `start-trip` and `finish-trip`.

**Frontend impact:** 
- Don't add "Mark Delivered" button per order
- Don't show individual order delivery timestamps
- All orders are marked delivered together

**Future improvement:** Phase 8 will add per-order tracking.

---

### tripDeliveryStatus Updates Only via Start/Finish

| Action | tripDeliveryStatus Change |
|--------|---------------------------|
| Create trip | All orders → NOT_VISITED |
| Start trip | All orders → NOT_VISITED (no change) |
| Finish trip | All orders → VISITED |

**Frontend impact:** Don't try to update `tripDeliveryStatus` via other APIs. It won't work.

---

### No Partial Delivery

**Scenario:** Shipper delivers 3 out of 5 orders, cannot complete the rest.

**MVP limitation:** Cannot mark partial completion. Must either:
- Finish entire trip (all 5 orders marked DELIVERED)
- Cancel trip (all 5 orders remain READY)

**Frontend impact:** Don't add "Complete Partial" option.

---

### No Realtime Location Tracking

**MVP does not include:**
- Live shipper location sharing
- Customer tracking "where is my order?"
- Estimated arrival time updates

**Frontend impact:** Don't implement map with moving shipper marker (yet).

---

## 7. Common Mistakes for Frontend

### ❌ Mistake 1: Using Order List Order Instead of stopIndex

**Wrong:**
```kotlin
// Don't do this!
trip.orders.forEachIndexed { index, order ->
    println("Stop ${index + 1}: ${order.buildingCode}")
}
```

**Correct:**
```kotlin
trip.waypoints.forEach { waypoint ->
    println("Stop ${waypoint.order}: ${waypoint.buildingCode}")
}
```

**Why:** Orders array is not sorted by visit sequence. Use waypoints or stopIndex.

---

### ❌ Mistake 2: Assuming tripDeliveryStatus Updates Per Stop

**Wrong assumption:** "When shipper reaches Stop 1, I'll call an API to update those orders' tripDeliveryStatus."

**Reality:** No such API exists in MVP. All orders update together when trip finishes.

---

### ❌ Mistake 3: Forgetting Max 15 Orders

**Wrong:**
```kotlin
// No validation!
createTrip(orderIds = selectedOrders)
```

**Correct:**
```kotlin
if (selectedOrders.size > 15) {
    showError("Maximum 15 orders per trip")
    return
}
createTrip(orderIds = selectedOrders)
```

Backend will reject with 400 error, but better to validate in UI first.

---

### ❌ Mistake 4: Not Filtering Orders by READY

**Wrong:** Show all orders (PENDING, READY, SHIPPING, DELIVERED).

**Correct:** Only show READY orders in trip creation screen.

**Reason:** Backend validates orders must be READY. Other statuses will cause 400 error.

---

### ❌ Mistake 5: Expecting finish-trip to Return Just Trip

**Wrong:**
```kotlin
val trip = response.data // Error! data is not a Trip object
```

**Correct:**
```kotlin
val trip = response.data.trip
val count = response.data.ordersDelivered
```

`finish-trip` endpoint returns `{ trip, ordersDelivered }`, not just trip.

---

### ❌ Mistake 6: Building Name Instead of buildingCode

**Wrong:**
```json
{
  "building": "Tòa A1" // Vietnamese name
}
```

**Correct:**
```json
{
  "buildingCode": "A1" // Code only
}
```

Backend expects codes: A1, A2, B1, B2, C3, etc. Not full names.

---

### ❌ Mistake 7: Trying to Cancel a STARTED Trip

**Wrong:** Show "Cancel Trip" button for all trips.

**Correct:**
```kotlin
if (trip.status == TripStatus.PENDING) {
    Button(onClick = { cancelTrip() }) { Text("Cancel Trip") }
}
```

Only PENDING trips can be cancelled. Backend returns 409 Conflict otherwise.

---

### ❌ Mistake 8: Parsing Timestamps as Objects

**Wrong:**
```kotlin
// Expecting Firestore Timestamp object
val createdAt = trip.createdAt._seconds
```

**Correct:**
```kotlin
// Timestamps are ISO-8601 strings
val instant = Instant.parse(trip.createdAt) // "2026-01-29T10:00:00.000Z"
```

All timestamps in API responses are ISO-8601 strings, not Firestore objects.

---

## 8. Quick Checklist for Frontend Integration

### Before Release

#### ✅ API Integration

- [ ] Can create trip from 1-15 orders
- [ ] Can retrieve trip by ID
- [ ] Can list trips with pagination
- [ ] Can start trip (PENDING → STARTED)
- [ ] Can finish trip (STARTED → FINISHED)
- [ ] Can cancel trip (PENDING → CANCELLED)
- [ ] Handle finish-trip special response format `{ trip, ordersDelivered }`

#### ✅ Validation

- [ ] Block trip creation if > 15 orders selected
- [ ] Block trip creation if any order missing buildingCode
- [ ] Only show "Start Trip" button when status is PENDING
- [ ] Only show "Finish Trip" button when status is STARTED
- [ ] Only show "Cancel Trip" button when status is PENDING
- [ ] Filter orders list to show only READY status

#### ✅ Map & Route

- [ ] Decode and render polyline on map
- [ ] Show origin marker (green)
- [ ] Show waypoint markers (blue) with stop numbers
- [ ] Auto-fit map bounds to show entire route
- [ ] Display waypoints list in correct order (use waypoints array, not orders array)

#### ✅ Error Handling

- [ ] Handle 400 errors (validation failures) with user-friendly messages
- [ ] Handle 401 errors (redirect to login)
- [ ] Handle 403 errors (permission denied)
- [ ] Handle 404 errors (trip/order not found)
- [ ] Handle 409 errors (state transition errors like "Cannot start STARTED trip")
- [ ] Handle network errors (timeout, no internet)

#### ✅ UI/UX

- [ ] Show loading state during API calls
- [ ] Display total distance and duration from route
- [ ] Show order count per stop: "Stop 1: A1 (3 orders)"
- [ ] Display trip summary: "3 stops, 7 orders, 2.4 km, ~8 minutes"
- [ ] Show success message after trip finishes: "5 orders delivered!"
- [ ] Handle cases where totalBuildings < totalOrders (multiple orders same building)

#### ✅ Testing Scenarios

- [ ] Create trip with 1 order
- [ ] Create trip with 15 orders (max)
- [ ] Try creating trip with 16 orders (should fail validation)
- [ ] Create trip with orders going to same building (check stopIndex grouping)
- [ ] Start trip, then immediately finish (happy path)
- [ ] Try starting a STARTED trip (should fail with 409)
- [ ] Try canceling a STARTED trip (should fail with 409)
- [ ] Cancel a PENDING trip successfully
- [ ] Test pagination in trip list (load more)
- [ ] Test with orders missing buildingCode (should fail validation)

#### ✅ Known Limitations Handled

- [ ] No "Recalculate Route" button (static route MVP)
- [ ] No per-order "Mark Delivered" button (batch updates only)
- [ ] No partial delivery option (all or nothing)
- [ ] No realtime shipper location tracking (future feature)
- [ ] Explained to users: All orders marked delivered together when trip finishes

---

## Quick Reference: Trip State Transitions

```
            create-trip
                ↓
           [PENDING] ←────────── (Can cancel here)
                ↓ start-trip
                ↓
           [STARTED] ←────────── (Cannot cancel here)
                ↓ finish-trip
                ↓
          [FINISHED]


Orders:    READY → SHIPPING → DELIVERED
            ↑         ↑           ↑
            create    start     finish
```

---

## Need Help?

**Documentation:**
- API Swagger: Check backend Swagger UI for live API testing
- Backend audit report: `docs/reports/gps_swagger_examples_audit.md`

**Common Questions:**
1. **"Why is my route order different from input order?"**  
   → Route is optimized by Google Routes API. Always use `waypoints` array order.

2. **"Can I mark individual orders as delivered?"**  
   → Not in MVP. Use `finish-trip` to mark all orders delivered together.

3. **"What if shipper can only deliver 3 out of 5 orders?"**  
   → MVP doesn't support partial delivery. Either finish all or cancel entire trip.

4. **"Can I recalculate the route mid-trip?"**  
   → Not in MVP. Route is static after creation.

5. **"Why does totalBuildings not equal totalOrders?"**  
   → Multiple orders can go to the same building, creating one waypoint for multiple orders.

---

**End of Guide**
