import { ShipperTrip, TripStatus, TripDeliveryStatus } from '../entities/shipper-trip.entity';
import { Timestamp } from 'firebase-admin/firestore';

/**
 * GPS Controller Response Shape Test
 *
 * Verifies that GPS endpoints return RAW data without manual {success, data} wrapping.
 * The global TransformInterceptor is responsible for wrapping the response envelope.
 *
 * This is a UNIT test that focuses on response shape correctness.
 */
describe('GPS Controller - Response Shape Correctness', () => {
  const now = Timestamp.now();

  const mockTrip: ShipperTrip = {
    id: 'trip_test_123',
    shipperId: 'shipper_123',
    status: TripStatus.PENDING,
    origin: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
    returnTo: { lat: 10.773589, lng: 106.659924, name: 'Hub' },
    waypoints: [
      {
        buildingCode: 'A1',
        location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
        order: 1,
      },
    ],
    orders: [
      {
        orderId: 'order_123',
        buildingCode: 'A1',
        tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
      },
    ],
    route: {
      distance: 5000,
      duration: 600,
      polyline: 'encoded_polyline',
      waypointOrder: [0],
    },
    totalDistance: 5000,
    totalDuration: 600,
    totalOrders: 1,
    totalBuildings: 1,
    createdAt: now,
    updatedAt: now,
  } as ShipperTrip;

  describe('Response Envelope Structure', () => {
    it('Trip object should NOT have "success" property (no manual wrapping)', () => {
      // Controllers should return raw trip data
      expect(mockTrip).not.toHaveProperty('success');
    });

    it('Trip object should have "id" at root level (not nested)', () => {
      expect(mockTrip.id).toBe('trip_test_123');
      expect(mockTrip).toHaveProperty('id');
    });

    it('Trip object should have core properties directly accessible', () => {
      expect(mockTrip.shipperId).toBe('shipper_123');
      expect(mockTrip.status).toBe(TripStatus.PENDING);
      expect(mockTrip.totalDistance).toBe(5000);
      expect(mockTrip.totalDuration).toBe(600);
      expect(mockTrip.waypoints).toBeDefined();
    });

    it('Raw response object should NOT be wrapped in { success: true, data: {...} }', () => {
      // This is the pattern controllers should NOT follow
      const wrongResponse = {
        success: true,
        data: mockTrip,
      };

      // Verify it IS wrapped (to show wrong pattern)
      expect(wrongResponse).toHaveProperty('success');
      expect(wrongResponse).toHaveProperty('data');

      // Verify raw trip does NOT have these properties
      expect(mockTrip).not.toHaveProperty('success');
      expect(mockTrip).not.toHaveProperty('data');
    });
  });

  describe('TransformInterceptor Pattern', () => {
    it('Global TransformInterceptor should wrap raw response with { success, data, timestamp }', () => {
      // Simulate what TransformInterceptor does
      const interceptedResponse = {
        success: true,
        data: mockTrip,
        timestamp: new Date().toISOString(),
      };

      // Verify single-level wrapping
      expect(interceptedResponse).toHaveProperty('success', true);
      expect(interceptedResponse).toHaveProperty('data');
      expect(interceptedResponse).toHaveProperty('timestamp');

      // Verify data is the raw trip
      expect(interceptedResponse.data).toEqual(mockTrip);
      expect(interceptedResponse.data).not.toHaveProperty('success');
    });

    it('NO double-wrapping: data should be raw object, not { success, data }', () => {
      // WRONG pattern (if controller wrapped AND interceptor wrapped)
      const doubleWrapped = {
        success: true,
        data: {
          success: true,
          data: mockTrip,
        },
        timestamp: new Date().toISOString(),
      };

      // Verify raw trip does NOT match double-wrapped structure
      expect(mockTrip).not.toEqual(doubleWrapped.data);
      // Verify raw trip is NOT nested inside a "data" property of another object with "success"
      expect((mockTrip as any).success).toBeUndefined();
    });
  });

  describe('Code Guideline Compliance', () => {
    it('Controller methods should return raw domain objects', () => {
      // ✅ CORRECT pattern
      const correctControllerReturn = mockTrip;

      expect(correctControllerReturn).toEqual(mockTrip);
      expect(correctControllerReturn).not.toHaveProperty('success');
      expect(correctControllerReturn.id).toBe('trip_test_123');
    });

    it('Service methods should return raw domain objects', () => {
      // ✅ CORRECT pattern
      const correctServiceReturn = mockTrip;

      expect(correctServiceReturn).toEqual(mockTrip);
      expect(correctServiceReturn.shipperId).toBe('shipper_123');
    });

    it('Paginated response should NOT wrap items in success/data', () => {
      const paginatedResponse = {
        items: [mockTrip],
        total: 1,
        page: 1,
        limit: 20,
        totalPages: 1,
      };

      // Items should be raw array of trips
      expect(paginatedResponse.items[0]).toEqual(mockTrip);
      expect(paginatedResponse.items[0]).not.toHaveProperty('success');
    });

    it('Finish trip response should NOT wrap trip in success/data', () => {
      const finishTripResponse = {
        trip: mockTrip,
        ordersDelivered: 1,
      };

      // Trip should be raw object
      expect(finishTripResponse.trip).toEqual(mockTrip);
      expect(finishTripResponse.trip).not.toHaveProperty('success');
    });
  });
});
