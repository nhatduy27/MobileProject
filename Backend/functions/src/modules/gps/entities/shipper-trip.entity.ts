/**
 * Shipper Trip Entity
 *
 * Represents a trip with optimized route for deliveries.
 */

/** Trip status */
export enum TripStatus {
  PENDING = 'PENDING',
  STARTED = 'STARTED',
  FINISHED = 'FINISHED',
  CANCELLED = 'CANCELLED',
}

/** Delivery status for each building/order in trip */
export enum TripDeliveryStatus {
  NOT_VISITED = 'NOT_VISITED',
  VISITED = 'VISITED',
  FAILED = 'FAILED',
}

/** Location with coordinates */
export interface TripLocation {
  lat: number;
  lng: number;
  name?: string;
}

/** Waypoint in optimized route */
export interface TripWaypoint {
  buildingCode: string;
  location: TripLocation;
  order: number; // 1-based visiting order (1st stop, 2nd stop, 3rd stop...)
}

/** Order associated with trip */
export interface TripOrder {
  orderId: string;
  buildingCode: string;
  tripDeliveryStatus: TripDeliveryStatus;
  stopIndex: number; // 1-based stop number matching waypoint.order (maps order to waypoint)
}

/** Route optimization result */
export interface TripRoute {
  distance: number; // meters
  duration: number; // seconds
  polyline?: string; // encoded polyline (optional)
  waypointOrder: number[]; // Sequential [0,1,2,...] - waypoints array is already in optimized visiting order
}

/** Shipper Trip Document */
export interface ShipperTrip {
  /** Trip ID (auto-generated) */
  id: string;

  /** Shipper user ID */
  shipperId: string;

  /** Trip status */
  status: TripStatus;

  /** Starting location (shipper's location) */
  origin: TripLocation;

  /** Return destination (hub/gate or same as origin) */
  returnTo: TripLocation;

  /** Optimized waypoints in visiting order (waypoints[0] = 1st stop, waypoints[1] = 2nd stop, etc.) */
  waypoints: TripWaypoint[];

  /** Associated orders */
  orders: TripOrder[];

  /** Route optimization data */
  route: TripRoute;

  /** Total distance (meters) */
  totalDistance: number;

  /** Estimated duration (seconds) */
  totalDuration: number;

  /** Total number of orders */
  totalOrders: number;

  /** Total number of unique buildings */
  totalBuildings: number;

  /** Trip creation timestamp */
  createdAt: FirebaseFirestore.Timestamp;

  /** Last update timestamp */
  updatedAt: FirebaseFirestore.Timestamp;

  /** Trip start timestamp (when shipper starts) */
  startedAt?: FirebaseFirestore.Timestamp;

  /** Trip finish timestamp (when shipper completes) */
  finishedAt?: FirebaseFirestore.Timestamp;

  /** Trip cancellation timestamp */
  cancelledAt?: FirebaseFirestore.Timestamp;

  /** Reason for cancellation */
  cancelReason?: string;

  /**
   * TODO (GPS-011 - Phase 8): Realtime Location Tracking
   *
   * Add the following fields for live shipper tracking:
   *
   * currentLocation?: TripLocation;
   * lastLocationUpdate?: FirebaseFirestore.Timestamp;
   *
   * Implementation notes:
   * - Mobile app sends location every 30 seconds while trip STARTED
   * - Add POST /api/gps/update-location endpoint
   * - Firestore rules: allow shipper to update own currentLocation
   * - Frontend: Display shipper marker on map with ETA updates
   * - Consider geofencing for auto-mark delivery when near building
   */
}
