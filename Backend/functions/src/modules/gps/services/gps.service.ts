import { Injectable, Logger, BadRequestException, NotFoundException, ForbiddenException, ConflictException } from '@nestjs/common';
import { GoogleRoutesService } from './google-routes.service';
import { DeliveryPointsRepository } from '../repositories/delivery-points.repository';
import { ShipperTripsRepository } from '../repositories/shipper-trips.repository';
import { OrdersService } from '../../orders/services/orders.service';
import { DeliveryPointsService } from '../../delivery-points/services/delivery-points.service';
import {
  ShipperTrip,
  TripStatus,
  TripDeliveryStatus,
  TripWaypoint,
  TripOrder,
} from '../entities/shipper-trip.entity';
import { CreateOptimizedTripDto } from '../dto/create-optimized-trip.dto';
import { PaginatedTripsDto } from '../dto/paginated-trips.dto';

/**
 * GPS Service
 *
 * Core business logic for route optimization and trip management.
 */
@Injectable()
export class GpsService {
  private readonly logger = new Logger(GpsService.name);

  constructor(
    private readonly googleRoutesService: GoogleRoutesService,
    private readonly deliveryPointsRepository: DeliveryPointsRepository,
    private readonly shipperTripsRepository: ShipperTripsRepository,
    private readonly ordersService: OrdersService,
    private readonly deliveryPointsService: DeliveryPointsService,
  ) {}

  /**
   * Create optimized trip for shipper
   *
   * @param shipperId Shipper user ID
   * @param dto Trip creation data
   * @returns Created trip with optimized route
   */
  async createOptimizedTrip(shipperId: string, dto: CreateOptimizedTripDto): Promise<ShipperTrip> {
    this.logger.log(`Creating optimized trip for shipper ${shipperId} with ${dto.orderIds.length} orders`);

    // 1. Validate and fetch orders
    const orders = await this.validateOrders(dto.orderIds, shipperId);

    // 2. Extract building codes from orders
    const buildingCodes = this.extractBuildingCodes(orders);

    if (buildingCodes.length === 0) {
      throw new BadRequestException('No valid buildings found in orders. Ensure orders have building field.');
    }

    this.logger.log(`Extracted ${buildingCodes.length} unique buildings: ${buildingCodes.join(', ')}`);

    // 3. Fetch delivery points for buildings
    const deliveryPointsMap = await this.deliveryPointsRepository.getByBuildingCodes(buildingCodes);

    // Validate all buildings have delivery points
    const missingBuildings = buildingCodes.filter((code) => !deliveryPointsMap.has(code));
    if (missingBuildings.length > 0) {
      throw new BadRequestException(
        `Delivery points not found for buildings: ${missingBuildings.join(', ')}. ` +
          'Please ensure deliveryPoints collection is seeded.',
      );
    }

    // 4. Prepare waypoints for route optimization
    const waypoints = buildingCodes.map((code) => {
      const point = deliveryPointsMap.get(code)!;
      return {
        lat: point.location.lat,
        lng: point.location.lng,
      };
    });

    // 5. Determine return destination (use origin if not specified)
    const returnTo = dto.returnTo || dto.origin;

    // 6. Call Google Routes API for route optimization
    this.logger.log('Calling Google Routes API for optimization...');
    const routeResult = await this.googleRoutesService.computeOptimizedRoute(dto.origin, waypoints, returnTo);

    // 7. Build optimized waypoints with correct order
    const optimizedWaypoints: TripWaypoint[] = routeResult.waypointOrder.map((originalIndex, newOrder) => {
      const buildingCode = buildingCodes[originalIndex];
      const point = deliveryPointsMap.get(buildingCode)!;

      return {
        buildingCode,
        location: {
          lat: point.location.lat,
          lng: point.location.lng,
          name: point.name,
        },
        order: newOrder + 1, // 1-based order
      };
    });

    // 8. Map orders to trip orders with delivery status
    const tripOrders: TripOrder[] = orders.map((order) => ({
      orderId: order.id,
      buildingCode: order.deliveryAddress?.building || '',
      tripDeliveryStatus: TripDeliveryStatus.NOT_VISITED,
    }));

    // 9. Create trip document
    const tripData: Omit<ShipperTrip, 'id' | 'createdAt' | 'updatedAt'> = {
      shipperId,
      status: TripStatus.PENDING,
      origin: dto.origin,
      returnTo,
      waypoints: optimizedWaypoints,
      orders: tripOrders,
      route: {
        distance: routeResult.distance,
        duration: routeResult.duration,
        polyline: routeResult.polyline,
        waypointOrder: routeResult.waypointOrder,
      },
      totalDistance: routeResult.distance,
      totalDuration: routeResult.duration,
      totalOrders: orders.length,
      totalBuildings: buildingCodes.length,
    };

    // 10. Save to Firestore
    const trip = await this.shipperTripsRepository.create(tripData);

    this.logger.log(
      `Trip created: ${trip.id}, distance: ${trip.totalDistance}m, ` +
        `duration: ${trip.totalDuration}s, buildings: ${trip.totalBuildings}`,
    );

    return trip;
  }

  /**
   * Validate orders exist and are eligible for trip
   *
   * Orders must:
   * - Exist
   * - Not be cancelled or already delivered
   * - Have delivery address with building field
   *
   * Note: Using getShipperOrderDetail for validation
   */
  private async validateOrders(orderIds: string[], shipperId: string): Promise<any[]> {
    const orders = [];

    for (const orderId of orderIds) {
      try {
        // Fetch order detail (shipper view - checks if order is assigned to shipper)
        const order = await this.ordersService.getShipperOrderDetail(shipperId, orderId);

        // Validate order status
        if (order.status === 'CANCELLED') {
          throw new BadRequestException(`Order ${orderId} is cancelled and cannot be included in trip`);
        }

        if (order.status === 'DELIVERED') {
          throw new BadRequestException(`Order ${orderId} is already delivered`);
        }

        // Validate has building field
        if (!order.deliveryAddress?.building) {
          throw new BadRequestException(
            `Order ${orderId} does not have building field in delivery address. ` +
              'Building is required for route optimization.',
          );
        }

        orders.push(order);
      } catch (error) {
        // If order not found or other error, re-throw with context
        if (error instanceof NotFoundException) {
          throw new NotFoundException(`Order ${orderId} not found or not assigned to you`);
        }
        throw error;
      }
    }

    return orders;
  }

  /**
   * Extract unique building codes from orders
   */
  private extractBuildingCodes(orders: any[]): string[] {
    const buildingSet = new Set<string>();

    for (const order of orders) {
      const building = order.deliveryAddress?.building;
      if (building && typeof building === 'string') {
        buildingSet.add(building.trim().toUpperCase());
      }
    }

    return Array.from(buildingSet);
  }

  /**
   * Get single trip by ID
   *
   * @param shipperId Shipper user ID
   * @param tripId Trip ID
   * @returns Trip details
   */
  async getMyTrip(shipperId: string, tripId: string): Promise<ShipperTrip> {
    const trip = await this.shipperTripsRepository.getById(tripId);

    if (!trip) {
      throw new NotFoundException('Trip not found');
    }

    // Verify ownership
    if (trip.shipperId !== shipperId) {
      throw new ForbiddenException('You do not have permission to view this trip');
    }

    return trip;
  }

  /**
   * List trips for shipper with pagination
   *
   * @param shipperId Shipper user ID
   * @param status Optional status filter
   * @param page Page number (1-based)
   * @param limit Items per page
   * @returns Paginated trips
   */
  async listMyTrips(
    shipperId: string,
    status?: TripStatus,
    page: number = 1,
    limit: number = 20,
  ): Promise<PaginatedTripsDto> {
    // Validate pagination params
    const validPage = Math.max(1, page);
    const validLimit = Math.min(50, Math.max(1, limit));

    this.logger.log(`Listing trips for shipper ${shipperId}: page=${validPage}, limit=${validLimit}, status=${status || 'all'}`);

    // Get paginated results and total count
    const { trips, total } = await this.shipperTripsRepository.getByShipperIdPaginated(
      shipperId,
      validPage,
      validLimit,
      status,
    );

    // Calculate pagination metadata
    const totalPages = Math.ceil(total / validLimit);
    const hasNext = validPage < totalPages;

    return {
      items: trips,
      page: validPage,
      limit: validLimit,
      total,
      totalPages,
      hasNext,
    };
  }

  /**
   * Start a trip (PENDING -> STARTED)
   *
   * @param shipperId Shipper user ID
   * @param tripId Trip ID
   * @returns Updated trip
   */
  async startTrip(shipperId: string, tripId: string): Promise<ShipperTrip> {
    const trip = await this.getMyTrip(shipperId, tripId);

    // Validate state transition
    if (trip.status !== TripStatus.PENDING) {
      throw new ConflictException(
        `Cannot start trip. Trip is already ${trip.status}. Only PENDING trips can be started.`,
      );
    }

    // Update trip status
    await this.shipperTripsRepository.update(tripId, {
      status: TripStatus.STARTED,
      startedAt: new Date() as any, // Will be replaced by serverTimestamp in repo
    });

    this.logger.log(`Trip ${tripId} started by shipper ${shipperId}`);

    // Return updated trip
    return this.getMyTrip(shipperId, tripId);
  }

  /**
   * Finish a trip (STARTED -> FINISHED)
   *
   * @param shipperId Shipper user ID
   * @param tripId Trip ID
   * @returns Updated trip with delivery stats
   */
  async finishTrip(shipperId: string, tripId: string): Promise<{ trip: ShipperTrip; ordersDelivered: number }> {
    const trip = await this.getMyTrip(shipperId, tripId);

    // Validate state transition
    if (trip.status !== TripStatus.STARTED) {
      throw new ConflictException(
        `Cannot finish trip. Trip is ${trip.status}. Only STARTED trips can be finished.`,
      );
    }

    // Count delivered orders (for stats)
    const ordersDelivered = trip.orders.filter(
      (order) => order.tripDeliveryStatus === TripDeliveryStatus.VISITED,
    ).length;

    // Update trip status
    await this.shipperTripsRepository.update(tripId, {
      status: TripStatus.FINISHED,
      finishedAt: new Date() as any, // Will be replaced by serverTimestamp in repo
    });

    this.logger.log(
      `Trip ${tripId} finished by shipper ${shipperId}. Orders delivered: ${ordersDelivered}/${trip.totalOrders}`,
    );

    // Return updated trip
    const updatedTrip = await this.getMyTrip(shipperId, tripId);
    return {
      trip: updatedTrip,
      ordersDelivered,
    };
  }

  /**
   * @deprecated Proxy to shared DeliveryPointsService
   * Use DeliveryPointsService directly instead
   */
  async listActiveDeliveryPoints() {
    return this.deliveryPointsService.listActiveDeliveryPoints();
  }
}
