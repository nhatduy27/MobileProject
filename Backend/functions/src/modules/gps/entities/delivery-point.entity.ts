/**
 * Delivery Point Entity
 *
 * Represents a fixed delivery point (KTX building) with coordinates.
 */

/** Location with GPS coordinates */
export interface TripLocation {
  lat: number;   // Latitude
  lng: number;   // Longitude
}

export interface DeliveryPoint {
  /** Building code (e.g., "A1", "B5") */
  id: string;

  /** Building code (same as id) */
  buildingCode: string;

  /** Building name (e.g., "Tòa A1") */
  name: string;

  /** Location with GPS coordinates */
  location: TripLocation;

  /** Description/notes (Vietnamese) */
  note?: string;

  /** Whether this delivery point is active */
  active: boolean;

  /** Creation timestamp */
  createdAt: FirebaseFirestore.Timestamp;

  /** Last update timestamp */
  updatedAt: FirebaseFirestore.Timestamp;
}
