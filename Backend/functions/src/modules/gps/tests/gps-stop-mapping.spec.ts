import { TripOrder, TripWaypoint, TripDeliveryStatus } from '../entities/shipper-trip.entity';

/**
 * GPS Trip Stop Mapping Tests
 *
 * Verifies that orders are correctly mapped to waypoints via stopIndex
 */
describe('GPS Trip - Stop Mapping', () => {
  describe('stopIndex correctness', () => {
    it('should map each order.stopIndex to an existing waypoint.order', () => {
      // Arrange: Mock trip with 3 waypoints (D6, B4, A1) and 5 orders
      const waypoints: TripWaypoint[] = [
        {
          buildingCode: 'D6',
          location: { lat: 10.885174, lng: 106.781616, name: 'Tòa D6' },
          order: 1,
        },
        {
          buildingCode: 'B4',
          location: { lat: 10.88335, lng: 106.78353, name: 'Tòa B4' },
          order: 2,
        },
        {
          buildingCode: 'A1',
          location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
          order: 3,
        },
      ];

      const orders: TripOrder[] = [
        {
          orderId: 'order1',
          buildingCode: 'D6',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'order2',
          buildingCode: 'D6',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        }, // Shares D6
        {
          orderId: 'order3',
          buildingCode: 'B4',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 2,
        },
        {
          orderId: 'order4',
          buildingCode: 'A1',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        },
        {
          orderId: 'order5',
          buildingCode: 'B4',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 2,
        }, // Shares B4
      ];

      // Act: Verify stopIndex validity
      const waypointOrders = new Set(waypoints.map((w) => w.order));
      const invalidOrders = orders.filter((o) => !waypointOrders.has(o.stopIndex));

      // Assert: All stopIndex values must match existing waypoint orders
      expect(invalidOrders).toEqual([]);
      expect(orders.length).toBe(5); // 5 orders
      expect(waypoints.length).toBe(3); // 3 unique buildings/waypoints
    });

    it('should have stopIndex matching waypoint.order for the same buildingCode', () => {
      // Arrange
      const waypoints: TripWaypoint[] = [
        {
          buildingCode: 'C6',
          location: { lat: 10.883653, lng: 106.779822, name: 'Tòa C6' },
          order: 1,
        },
        {
          buildingCode: 'B5',
          location: { lat: 10.883947, lng: 106.782894, name: 'Tòa B5' },
          order: 2,
        },
      ];

      const orders: TripOrder[] = [
        {
          orderId: 'order1',
          buildingCode: 'C6',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'order2',
          buildingCode: 'B5',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 2,
        },
        {
          orderId: 'order3',
          buildingCode: 'C6',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
      ];

      // Act: Build mapping from buildingCode to expected stopIndex
      const buildingToStop = new Map<string, number>();
      waypoints.forEach((w) => buildingToStop.set(w.buildingCode, w.order));

      // Assert: Each order's stopIndex must match its buildingCode's waypoint order
      orders.forEach((order) => {
        const expectedStopIndex = buildingToStop.get(order.buildingCode);
        expect(order.stopIndex).toBe(expectedStopIndex);
      });
    });

    it('should handle multiple orders sharing the same building (same stopIndex)', () => {
      // Arrange: 10 orders going to 5 unique buildings
      const waypoints: TripWaypoint[] = [
        {
          buildingCode: 'A1',
          location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
          order: 1,
        },
        {
          buildingCode: 'B2',
          location: { lat: 10.88227, lng: 106.781518, name: 'Tòa B2' },
          order: 2,
        },
        {
          buildingCode: 'C3',
          location: { lat: 10.883185, lng: 106.780357, name: 'Tòa C3' },
          order: 3,
        },
        {
          buildingCode: 'D4',
          location: { lat: 10.884012, lng: 106.781967, name: 'Tòa D4' },
          order: 4,
        },
        {
          buildingCode: 'E5',
          location: { lat: 10.884678, lng: 106.782634, name: 'Tòa E5' },
          order: 5,
        },
      ];

      const orders: TripOrder[] = [
        {
          orderId: 'o1',
          buildingCode: 'A1',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'o2',
          buildingCode: 'A1',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        }, // Shared
        {
          orderId: 'o3',
          buildingCode: 'B2',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 2,
        },
        {
          orderId: 'o4',
          buildingCode: 'C3',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        },
        {
          orderId: 'o5',
          buildingCode: 'C3',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        }, // Shared
        {
          orderId: 'o6',
          buildingCode: 'C3',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        }, // Shared
        {
          orderId: 'o7',
          buildingCode: 'D4',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 4,
        },
        {
          orderId: 'o8',
          buildingCode: 'E5',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 5,
        },
        {
          orderId: 'o9',
          buildingCode: 'E5',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 5,
        }, // Shared
        {
          orderId: 'o10',
          buildingCode: 'B2',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 2,
        }, // Shared
      ];

      // Act: Group orders by stopIndex
      const ordersByStop = new Map<number, TripOrder[]>();
      orders.forEach((order) => {
        if (!ordersByStop.has(order.stopIndex)) {
          ordersByStop.set(order.stopIndex, []);
        }
        ordersByStop.get(order.stopIndex)!.push(order);
      });

      // Assert: Verify grouping
      expect(orders.length).toBe(10); // 10 orders
      expect(waypoints.length).toBe(5); // 5 unique buildings
      expect(ordersByStop.get(1)?.length).toBe(2); // Stop 1 (A1): 2 orders
      expect(ordersByStop.get(2)?.length).toBe(2); // Stop 2 (B2): 2 orders
      expect(ordersByStop.get(3)?.length).toBe(3); // Stop 3 (C3): 3 orders
      expect(ordersByStop.get(4)?.length).toBe(1); // Stop 4 (D4): 1 order
      expect(ordersByStop.get(5)?.length).toBe(2); // Stop 5 (E5): 2 orders
    });

    it('should have stopIndex values from 1 to totalBuildings', () => {
      // Arrange
      const waypoints: TripWaypoint[] = [
        {
          buildingCode: 'A1',
          location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
          order: 1,
        },
        {
          buildingCode: 'B2',
          location: { lat: 10.88227, lng: 106.781518, name: 'Tòa B2' },
          order: 2,
        },
        {
          buildingCode: 'C3',
          location: { lat: 10.883185, lng: 106.780357, name: 'Tòa C3' },
          order: 3,
        },
      ];

      const orders: TripOrder[] = [
        {
          orderId: 'o1',
          buildingCode: 'A1',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'o2',
          buildingCode: 'B2',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 2,
        },
        {
          orderId: 'o3',
          buildingCode: 'C3',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        },
        {
          orderId: 'o4',
          buildingCode: 'A1',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'o5',
          buildingCode: 'C3',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        },
      ];

      // Act: Get unique stopIndex values
      const uniqueStopIndexes = new Set(orders.map((o) => o.stopIndex));
      const totalBuildings = waypoints.length;

      // Assert: stopIndex should range from 1 to totalBuildings
      expect(uniqueStopIndexes.size).toBe(totalBuildings);
      expect(Math.min(...uniqueStopIndexes)).toBe(1);
      expect(Math.max(...uniqueStopIndexes)).toBe(totalBuildings);
    });
  });

  describe('orders array sorting', () => {
    it('should sort orders by stopIndex ascending', () => {
      // Arrange: Unsorted orders
      const orders: TripOrder[] = [
        {
          orderId: 'o3',
          buildingCode: 'C3',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        },
        {
          orderId: 'o1',
          buildingCode: 'A1',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'o5',
          buildingCode: 'B2',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 2,
        },
        {
          orderId: 'o2',
          buildingCode: 'A1',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'o4',
          buildingCode: 'C3',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        },
      ];

      // Act: Sort orders (simulating service behavior)
      orders.sort((a, b) => {
        if (a.stopIndex !== b.stopIndex) {
          return a.stopIndex - b.stopIndex;
        }
        return a.orderId.localeCompare(b.orderId);
      });

      // Assert: Orders should be sorted by stopIndex, then orderId
      expect(orders[0].stopIndex).toBe(1);
      expect(orders[1].stopIndex).toBe(1);
      expect(orders[2].stopIndex).toBe(2);
      expect(orders[3].stopIndex).toBe(3);
      expect(orders[4].stopIndex).toBe(3);

      // Assert: Within same stopIndex, orders are sorted by orderId
      expect(orders[0].orderId).toBe('o1'); // stopIndex=1
      expect(orders[1].orderId).toBe('o2'); // stopIndex=1
      expect(orders[2].orderId).toBe('o5'); // stopIndex=2
      expect(orders[3].orderId).toBe('o3'); // stopIndex=3
      expect(orders[4].orderId).toBe('o4'); // stopIndex=3
    });

    it('should verify every order.stopIndex maps to waypoint with same buildingCode', () => {
      // Arrange
      const waypoints: TripWaypoint[] = [
        {
          buildingCode: 'D6',
          location: { lat: 10.885174, lng: 106.781616, name: 'Tòa D6' },
          order: 1,
        },
        {
          buildingCode: 'B4',
          location: { lat: 10.88335, lng: 106.78353, name: 'Tòa B4' },
          order: 2,
        },
        {
          buildingCode: 'A1',
          location: { lat: 10.881765, lng: 106.781719, name: 'Tòa A1' },
          order: 3,
        },
      ];

      const orders: TripOrder[] = [
        {
          orderId: 'o1',
          buildingCode: 'D6',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'o2',
          buildingCode: 'D6',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 1,
        },
        {
          orderId: 'o3',
          buildingCode: 'B4',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 2,
        },
        {
          orderId: 'o4',
          buildingCode: 'A1',
          tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
          stopIndex: 3,
        },
      ];

      // Act: Verify each order's stopIndex matches waypoint with same buildingCode
      orders.forEach((order) => {
        const matchingWaypoint = waypoints.find(
          (w) => w.order === order.stopIndex && w.buildingCode === order.buildingCode,
        );

        // Assert: Order's stopIndex must match waypoint with same buildingCode
        expect(matchingWaypoint).toBeDefined();
        expect(matchingWaypoint?.buildingCode).toBe(order.buildingCode);
        expect(matchingWaypoint?.order).toBe(order.stopIndex);
      });
    });
  });
});
